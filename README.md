# Deliverables

Presentation Slides: https://docs.google.com/presentation/d/1YAJR2flF3P59fb_ojZiQZAOaFw-aOG20trYcM_r8Xhk/edit?usp=sharing
Tutorial Video: https://drive.google.com/file/d/1ya5x2sIyLwmeqxsMu1I_kFUR6VLPlDxG/view?usp=sharing
Tutorial Video + Slides: https://drive.google.com/file/d/10dVe4IRqOmVs3Z9ZuN2IYW27J6YTeDPi/view?usp=sharing

# Making a Custom Model
To get started with your own image recognition software you will have to start a new python project and import these libraries.

```python
import tensorflow as tf
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras import layers, models
from tensorflow.keras.preprocessing.image import ImageDataGenerator
```

tf is the core engine. MobileNetV2 is a pre-trained image model. layers and models let us build on top of the pre-trained model. ImageDataGenerator loads images and applied augmentations.

Next we need to rescale pixels, rotate and flip images and split train and validations sets.
Why rescale? neural networks train better when inputs are small floats compared to large integers.
Why augment? Rotation and flipping artificially enhances our dataset. The model sees each image from different angles, making our model more robust.

```python
train_datagen = ImageDataGenerator(
    rescale=1./255,       # pixel values 0–255 → 0.0–1.0
    rotation_range=20,    # randomly rotate up to 20°
    horizontal_flip=True, # randomly mirror images
    validation_split=0.2  # hold out 20% for validation
)
```

Next we need to make a stream of images from our custom dataset.

Folder structure expected "dataset/image_class/image"

We also split the images into 2 sets. A training set and a validation set.

Training set: The larger portion is used to teach the model to identify patterns, shapes, and features in the images.
Validation set: The smaller potion used to evaluate the model during training. Allows for fine tuning parameters to improve accuracy with never seen data.

```python
train_gen = train_datagen.flow_from_directory(
    "./your_custom_dataset",
    target_size=(224, 224), # resize all images to 224×224
    batch_size=32,          # feed 32 images at a time
    subset="training"       # use the 80% training split
)

val_gen = train_datagen.flow_from_directory(
    "./your_custom_dataset",
    target_size=(224, 224),
    batch_size=32,
    subset="validation"     # use the 20% validation split
)
```

Load MobileNetV2

We are going to reuse a pre-trained model to help improve our accuracy. This is a technique called Transfer learning.

Transfer Learning: A technique where a model developed for one task is reused as the starting point for a second related task.

In our case we are taking the MobileNetV2 model that has already been trained on millions of images. Then taking those skills and only teaching it the custom dataset part.

```python
base_model = MobileNetV2(
    weights="imagenet",    # start with ImageNet weights
    include_top=False,     # remove the final classification layer
    input_shape=(224, 224, 3)
)
base_model.trainable = False  # freeze — don't update these weights
```
note: include_top=False removes the orginal 1000-class output head so we can input our own.
      trainable=False freezes the base so we don't accidentally destroy any learned features.


Build classification head

We are now going to add a small classification head tailored to our custom dataset.

```python
NUM_CLASSES = train_gen.num_classes

model = models.Sequential([
    base_model,
    layers.GlobalAveragePooling2D(), # collapse spatial features to a vector
    layers.Dense(128, activation="relu"),  # learn specific patterns
    layers.Dense(NUM_CLASSES, activation="softmax") # output one prob per class
])
```
This code takes the feature map from the pre-trained model and averages them to a flat 1280 dim vector.
It then uses Dense() as a thinking layer that learns features that matter in your classification.
softmax ensures all class probabilities sum to 1.0. Giving it a clean probability distribution.

Now we are gonna start the fun part! Training.

```python
model.compile(
    optimizer="adam",
    loss="categorical_crossentropy",
    metrics=["accuracy"]
)
model.fit(train_gen, validation_data=val_gen, epochs=10)
```

In this code we are going over our dataset for a set amount times. Tracking the accuarcy and loss as the model is learning.
With each epoch the models training and validation accuracy will increase but keep an eye out for validation accuracy plateauing. This will be a sign of overfitting.

note: adam is a standard adaptive optimizer meaning it automatically adjust learning rates for each parameter during training.
      loss is a metric used to measure disparity between predicted probabilities and actual labels across two or more categories.
      epochs is one complete pass of the entire training dataset.
      overfitting is when a model starts to memorize the dataset.


Next we are going to export the model.

```python
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open("your_model.tflite", "wb") as f:
    f.write(tflite_model)
```
We export the model so that it wont have to retrain everytime we want it to identify a image.
TFLite makes a compact model that runs on Android and IOS.
This export can be put into your program as your very own custom model.

# Saving the labels

```python
import json
with open("labels.json", "w") as f:
    json.dump(train_gen.class_indices, f)
```
save this alongside the model so that the output number match a custom class.

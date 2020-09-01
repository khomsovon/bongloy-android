# Bongloy Android SDK

The Bongloy Android SDK makes it quick and easy to build an excellent payment experience in your Android app.

## Installation

### Android Studio (or Gradle)

No need to clone the repository or download any files -- just add this line to your app's `build.gradle` inside the `dependencies` section:

#### Bongloy

```
implementation 'com.stripe:stripe-android:8.1.0'

implementation 'com.github.bongloy-community:bongloy-android:1.0.3'
```

And add it in your root build.gradle at the end of repositories:

```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

## Usage

### Using `setDefaultPublishableKey`

A publishable key is required to identify your website when communicating with Bongloy. Remember to replace the test key with your live key in production.

You can get all your keys from [your account page](https://sandbox.bongloy.com/dashboard/api_keys).

```java
new Bongloy(context).setDefaultPublishableKey("YOUR_PUBLISHABLE_KEY");
```

### Using `createToken`

```java
new Bongloy(context).createToken(
    new Card("6200000000000005", 01, 2020, "123"),
    "YOUR_PUBLISHABLE_KEY"
    tokenCallback
);
```

## Official Documentation

Documentation for Bongloy can be found on the [Bongloy website](https://www.bongloy.com/documentation).




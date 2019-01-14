## How to

To add this library into your build:

### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```javascript
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2. Add the dependency
```javascript
dependencies {
	        implementation 'com.github.ImaginationRoom:CryptoAndroidService:Tag'
	}
```
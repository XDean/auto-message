# auto-message
[![Build Status](https://travis-ci.org/XDean/auto-message.svg?branch=master)](https://travis-ci.org/XDean/auto-message)
[![codecov.io](http://codecov.io/github/XDean/auto-message/coverage.svg?branch=master)](https://codecov.io/gh/XDean/auto-message/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.XDean/auto-message/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.XDean/auto-message)

Get Message Keys Automatically

# Motivation

For internationalization, we use `ResouceBundle` or `MessageSource`(spring). 

we have *message.proeprties*

    hello.world=HELLO WORLD!

and do this in code

    resource.getMessage("hello.world", Locale.ENGLISH);
    
But it is easy to get typo when print "hello.world" twice.

How about get the string in type safe way? Use **Auto Message**!

# Usage

Add `@AutoMessage` at the `package-info` or any class of the package you want to generate `Messages` class. For example

```java
@AutoMessage(path="/message.properties")
package sample.message;
```

and you have the message file *message.properties*

```properties
hello.world=Hello World!
bye.boy=Bye Boy!
```

It will auto generate `Messages`

```java
package smaple.message;

public interface Messages {
  String HELLO_WORLD = "hello.world";
  String BYE_BOY = "bye.boy";
}
```

Then you can do `getMessage` in a type safe way:

    resouce.getMessage(Messages.HELLO_WORLD, Locale.ENGLISH);
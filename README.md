This is Systemi@LT
=====================================

Finance management application for Lateral-Thoughts
* Invoice management
* Budget management
* Business point management
* Statistics about financial condition of Lateral-Thoughts

This application aims to replace the google sheet currently used.

Technologies
-------

Systemi@LT use the following technologies :
* [Scala](http://www.scala-lang.org/)
  * [Play Framework](https://www.playframework.com/)
  * [Apache Lucene](http://lucene.apache.org/core/)
* [Javascript](http://en.wikipedia.org/wiki/JavaScript)
  * [AngularJS](https://angularjs.org/)
  * [Bootstrap](http://getbootstrap.com/)
* [MongoDB](http://www.mongodb.org/)
  * [ReactiveMongo](http://reactivemongo.org/)
  
How to run application
------

* [Install play2 framework version 2.3](https://www.playframework.com/documentation/2.3.x/Installing)
* [Install and run mongodb](http://docs.mongodb.org/manual/tutorial/install-mongodb-on-debian/) (debian)
* Clone project
```bash
$ git clone git@github.com:LateralThoughts/systemi.git
``` 
or

```bash
$ git clone https://github.com/LateralThoughts/systemi.git
``` 
* Go to systemi repository
```bash
$ cd systemi
```
* Launch application :
```bash 
$ activator run
```
* You can now go to http://localhost:9000 with your favorite browser to log in !

### How to run test

In the cloned repository, run the following command :
```bash
$ sbt test
```
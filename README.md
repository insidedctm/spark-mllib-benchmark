# spark-mllib-benchmark
Scripts and code to run Apache Spark MLLib benchmarks

## Overview
These benchmarks are intended to be run using AWS EC2 spark instances. First fire up a spark amazon instance with the number of slaves you want. You'll need to login to the master, upload the code, install the data and then run the benchmark.
This is the first release of the benchmark code which targets the MLlib Linear SVM classification model. Future releases of the code will add some of the other models. In addition I'll be adding the ability to scale the datasets to any size you want.

## Compiling the code
Clone this repo, e.g.

    git clone <github_url>

Then cd into the directory and run sbt

    cd ~/spark-mllib-benchmark
    sbt clean package

## Run Amazon instance
You'll need to have an AWS account and have created a keypair as documented in the [Running Spark on EC2](https://spark.apache.org/docs/1.2.0/ec2-scripts.html) section of the Apache Spark site.

Run up an instance like this, assuming you have set the AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables already

    ./spark-ec2 -k <name_of_your_key_pair> -i <path_to_ssh_private_key> -s 4 launch benchmark

This will launch an EC2 Spark cluster with 4 slave nodes

Login as usual e.g.

    ./spark-ec2 -k <name_of_your_key_pair> -i <path_to_ssh_private_key> login benchmark

Once you've finished running your benchmarks don't forget to tear down the cluster, otherwise you'll continue to rack up costs

    ./spark-ec2 -k <name_of_your_key_pair> -i <path_to_ssh_private_key> destroy benchmark

## Download the data
This benchmark uses the [rcv1.binary](http://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets/binary.html#rcv1.binary) dataset from the [libsvm datasets](http://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets) site. The following commands will download and unzip the data and push it into the HDFS instance that can be read by the Spark nodes

    mkdir ~/data
    cd ~/data
    wget http://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets/binary/rcv1_train.binary.bz2
    wget http://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets/binary/rcv1_test.binary.bz2
    bzip2 -d rcv1_train.binary.bz2
    bzip2 -d rcv1_test.binary.bz2 
    ~/ephemeral-hdfs/bin/hadoop fs -mkdir /data
    ~/ephemeral-hdfs/bin/hadoop fs -put rcv1_train.binary /data
    ~/ephemeral-hdfs/bin/hadoop fs -put rcv1_test.binary /data

Note. we use the testing set to train as it has many more rows than the training set:

| dataset  | size (rows)  |
|:---------|-------------:|
| test     | 677,399       |
| train    | 20,242        |

## Running the benchmark
Upload the application code _target/scala-2.10/classification-benchmark_2.10-1.0.jar_ to the master and run the benchmark

    cd ~/spark
    ./bin/spark-submit   --class benchmark.classification.Benchmark   --master <spark_url>  classification-benchmark_2.10-1.0.jar 

The time to train the model is show in the last line of output

    Elapsed time: 72200219417ns


## Caching
By default the training dataset RDD is cached. A _nocache_ option is available to see the effects of not running with cached datasets:

    ./bin/spark-submit   --class benchmark.classification.Benchmark   --master <spark_url>  classification-benchmark_2.10-1.0.jar nocache


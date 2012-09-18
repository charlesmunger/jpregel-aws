#!/bin/sh
cd Client-Resources
unzip apache-ant-1.8.4-bin.zip
echo export ANT_HOME=/home/ec2-user/jpregel-aws/Client-Resources/apache-ant-1.8.4 >> ~/.bashrc
echo export PATH=$PATH:/home/ec2-user/jpregel-aws/Client-Resources/apache-ant-1.8.4/bin >> ~/.bashrc
sudo yum install java-1.6.0-openjdk-devel
exec bash

cd Client-Resources
unzip apache-ant-1.8.4-bin.zip
export ANT_HOME=/home/ec2-user/jpregel-aws/Client-Resources/apache-ant-1.8.4-bin #This will point to the new version
export PATH=$PATH:/home/ec2-user/jpregel-aws/Client-Resources/apache-ant-1.8.4-bin
echo export ANT_HOME=/home/ec2-user/jpregel-aws/Client-Resources/apache-ant-1.8.4-bin >> ~/.bashrc
echo export PATH=$PATH:/home/ec2-user/jpregel-aws/Client-Resources/apache-ant-1.8.4-bin >> ~/.bashrc

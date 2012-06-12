jpregel-aws
==============

jPregel on AWS

This code is based on a project called jPregel, written by Peter Cappello, 
and a Java package to run jPregel on AWS, which was written by Varsha Parthasarathy. 
The latter package has been essentially rewritten by Charles Munger.

To deploy this code:

1. Create an Amazon Web Services account. This requires a credit card. 

2. Create two SSH key pairs, named "privatekey" and "masterkey". Save these into privatekey.pem and masterkey.pem, in the jpregel-aws directory. 

3. Create a file named key.AWSkey, with your access key on the first line and your secret key on the second.

Specify a Main file with ant:

ant run -propertyfile nbproject/configs/ShortestPath.properties -Dapplication.args="foobucket 10 10 true true"

This project is licensed under the MIT license, which is provided in license.txt.
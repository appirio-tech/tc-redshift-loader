---------------------
Overview:
---------------------
This utility is a loader that pumps challenge cost data at the line item level from Informix to Redshift. It currently loads the cost_transaction table in Redshift. In terms of data, it mirrors what the "Competition Costs" report (https://www.topcoder.com/direct/dashboardBillingCostReport.action) does in Topcoder Direct. 

This loader can be expanded to perform additional loads.  It can be more generically thought of as a loader from a database to S3 to Reshift.

SQLDB --> S3 --> COPY to Redshift

This loader will always do a full load each time. 


---------------------
PreRequisites:
---------------------
Maven
JDK 8

---------------------
Build and Run Steps :
---------------------



1. Navigate to maven project folder ('tc-informix-data-export'). Do `mvn package assembly:single` to build fat jar
2. Navigate to target folder, there you can see `tc-informix-data-export-0.1-jar-with-dependencies.jar` generated
3. Create a file `config.properties` with following contents

```
sourceInformix.dbhost=<docker-ip>
sourceInformix.dbport=<informix_port_1>
sourceInformix.dbuser=informix
sourceInformix.dbpass=1nf0rm1x
sourceInformix.dbserver=informixoltp_tcp
sourceInformix.schemaname=informix


config.csvSeparator=~
config.queriesDir=../cost_queries
config.csvTargetDirectory=/Users/maturus/Documents/topcoder/tc-informix-data-export
```
Make sure to replace '<docker-ip>' and 'informix_port_1'

To run source docker informix instance, I would suggest to use

`docker run -it -p <informix_port_1>:2021 appiriodevops/informix:1.2`

4. Step 3 has the queries that will be executed (where data is to be transferred from)
	By default, upon launching
	a) tcs_catalog.project_info
		Has the data already.
	b) informixoltp.invoice
		Doesn't have any data. I would suggest to generate random data using DBSchema or else use
		`informixoltp-invoice-testdata.sql` given in submission.zip to insert test data.
5. Run jar now using
	`java -jar tc-informix-data-export-0.1-jar-with-dependencies.jar <path_to_config.properties>`
	This should read the tables in sourceInformix and store it in files as per fileName in dbname.table_to_csv.mappings and in directory as per config param config.csvTargetDirectory

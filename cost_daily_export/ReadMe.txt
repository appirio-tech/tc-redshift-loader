# ScreenCast :
http://www.screencast.com/t/2ltq5ZpL

# Run Steps
1) Install postgresql client
In ubuntu you can simply follow https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-14-04
Usually, you need to install postgresql , through command such as following
```
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
```
2) Install aws-cli using http://docs.aws.amazon.com/cli/latest/userguide/installing.html
3) Once aws (cli) and psql are installed
Modify script.sh given in submission.zip

Fill all the fields with <>
```
REDSHIFT_HOST=<REDSHIFT_HOST>
REDSHIFT_PORT=<REDSHIFT_PORT>
REDSHIFT_DATABASE=<REDSHIFT_DATABASE>
REDSHIFT_USER=<REDSHIFT_USER>
REDSHIFT_TABLE_NAME=<REDSHIFT_TABLE_NAME>
REDSHIFT_PASSWORD=<REDSHIFT_PASSWORD>
S3_BUCKET=<S3_BUCKET>
AWS_ACCESS_KEY_ID=<AWS_ACCESS_KEY_ID>
AWS_SECRET_ACCESS_KEY=<AWS_SECRET_ACCESS_KEY>
```
Also change following variable, change it to any other if the name/path is changed from default
```
RUN_TOOL_CMD="java -jar tc-informix-data-export-0.1-jar-with-dependencies.jar config.properties"
PATH_TO_COST_TRANSACTIONS_FOLDER="`pwd`/cost_transactions"
```
4) Feed credentials in `~/.aws/credentials` like following
```
[default]
aws_access_key_id = <AWS_ACCESS_KEY_ID>
aws_secret_access_key = <AWS_SECRET_ACCESS_KEY>
```
5) Run script.sh with `sh script.sh`

# For Reviewer Testing
1) You can use `script_sample.sh` for testing export sql data and load csv data.
To do that
Make sure you change following parameters if cost_transactions folder isn't in script folder and also file `~/.aws/credentials`
```
PATH_TO_COST_TRANSACTIONS_FOLDER="/Users/maturus/Downloads/cost_transactions"
```

* Run following in informix
```
create table dummy
(
  id integer
)
```
* Load it with dummy data using `dummy_data.sql`
* Run following in redshift
```
create table dummy
(
  id integer
)
```

Now run the script with `sh script_sample.sh`
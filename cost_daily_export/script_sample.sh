#!/bin/bash
set -e
set -u

REDSHIFT_HOST="tc.csdw9py0vxmp.us-east-1.redshift.amazonaws.com"
REDSHIFT_PORT="5439"

REDSHIFT_DATABASE="tc"

REDSHIFT_USER="root"
REDSHIFT_TABLE_NAME="dummy"
REDSHIFT_PASSWORD="r3dSh!fT"

S3_BUCKET="30054411"

S3PATH=s3://$S3_BUCKET/dummy_data # {bucket-name}/{optional-prefix}{distribution-ID}

AWS_ACCESS_KEY_ID="AKIAIHKF2V2I34YEHD7A"
AWS_SECRET_ACCESS_KEY="87DtMpORJaFA/KQ8jroSa85JS+hGl8JvXgLuzvwx"

PATH_TO_COST_TRANSACTIONS_FOLDER="`pwd`/cost_transactions"
CSV_IMPORT_DIR=$PATH_TO_COST_TRANSACTIONS_FOLDER"/csv_dummy"

RUN_TOOL_CMD="java -jar tc-informix-data-export-0.1-jar-with-dependencies.jar config_dummy.properties"

echo "Emptying output folder $CSV_IMPORT_DIR"
rm -rf "$CSV_IMPORT_DIR"/*

echo "Navigating to $PATH_TO_COST_TRANSACTIONS_FOLDER"
cd "$PATH_TO_COST_TRANSACTIONS_FOLDER"
echo "Running Tool '$RUN_TOOL_CMD'"
echo "`$RUN_TOOL_CMD`"

echo "Copying files to S3"
aws s3 cp "$CSV_IMPORT_DIR" $S3PATH --recursive --include "*.csv"

echo "S3 Done";
# Secure temp files
export PGPASSFILE=$(mktemp /tmp/pass.XXXXXX)
cmds=$(mktemp /tmp/cmds.XXXXXX)

cat >$PGPASSFILE << EOF
$REDSHIFT_HOST:$REDSHIFT_PORT:$REDSHIFT_DATABASE:$REDSHIFT_USER:$REDSHIFT_PASSWORD
EOF

cat >$cmds << EOF
truncate table $REDSHIFT_TABLE_NAME;
COPY $REDSHIFT_TABLE_NAME
FROM '$S3PATH/'
CREDENTIALS 'aws_access_key_id=$AWS_ACCESS_KEY_ID;aws_secret_access_key=$AWS_SECRET_ACCESS_KEY'
DELIMITER '~'
timeformat 'auto'
ACCEPTANYDATE
EMPTYASNULL
REMOVEQUOTES
TRUNCATECOLUMNS
TRIMBLANKS
IGNOREHEADER AS 1
NULL AS '(null)';
EOF

psql -d $REDSHIFT_DATABASE -h $REDSHIFT_HOST -p $REDSHIFT_PORT -U $REDSHIFT_USER -w -f $cmds
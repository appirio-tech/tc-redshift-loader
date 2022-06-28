/*
 * Copyright (C) 2016 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import com.informix.jdbc.IfmxPreparedStatement;
import com.opencsv.CSVWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * Handles data export from database
 *
 * @author TCSASSEMBLER
 */
public class InformixDataExport {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // TODO: Get from config
    static {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.ALL);
        ctx.updateLoggers();
    }

    private static final Logger logger = LogManager.getLogger(InformixDataExport.class);

    /**
     * Extracts data from database based on a provided query
     *
     * @param informixDbInterface the database connection
     * @param query               the query to execute
     * @param csvSeparator        the csv separator
     * @param targetFileName      target file name
     * @param includeColumns      weather to include header row or not
     * @throws Exception if any error occurs
     */
    public static void informixExtractData(final DBInterface informixDbInterface, final String query, final char csvSeparator, final String targetFileName, boolean includeColumns) throws Exception {
        try (InformixDBConnect informixDBConnect = new InformixDBConnect(informixDbInterface); Connection conn = informixDBConnect.getNewConnection(); Statement stmt = conn.createStatement()) {
            logger.info("Default fetch size: {}", stmt.getFetchSize());
            conn.setNetworkTimeout(null, 0);
            stmt.setFetchSize(1000);
            logger.info("Updated default fetch size to 1000");
            try (ResultSet res = stmt.executeQuery(query); CSVWriter writer = new CSVWriter(new FileWriter(targetFileName, true), csvSeparator)) {
                logger.info("Query execution completed.");
                writer.writeAll(res, includeColumns);
                logger.info("Extracted data to file : " + targetFileName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void informixExtractDataInBatch(final DBInterface informixDBInterface, String query, final char csvSeparator, final String targetFileName, int fetchSize, String startDate, String endDate, int incrementBy) throws Exception {
        logger.info("Get records from {} to {}. Increment by {} days", startDate, endDate, incrementBy);
        logger.info("Acquiring connection....");
        long extractionStartTime = System.currentTimeMillis();

        try (InformixDBConnect dbConnect = new InformixDBConnect(informixDBInterface); Connection connection = dbConnect.getNewConnection()) {
            logger.info("Connected to database...");
            final long countReplacements = query.chars().filter(ch -> ch == '?').count();
            logger.info("Query requires {} replacements.", countReplacements);
            IfmxPreparedStatement statement = (IfmxPreparedStatement) connection.prepareStatement(query);
//            statement.setFetchSize(fetchSize);

            String dt = startDate;
            boolean done = false;
            boolean first = true;
            while (!done) {
                long queryTimeStart = System.currentTimeMillis();

                logger.info("Start Date: {}", dt);

                // Not all queries require "?" substitution - for such queries we get all the results in one go
                // and break out of the loop
                if (countReplacements == 0) {
                    done = true;
                    logger.info("No date replacements required. Will get all data in one go.");
                }

                if (countReplacements >= 1) {
                    statement.setString(1, dt);
                }

                if (countReplacements >= 3) {
                    statement.setString(3, dt);
                }

                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                Calendar c = Calendar.getInstance();
                c.setTime(sdf.parse(dt));
                c.add(Calendar.DATE, incrementBy);  // number of days to add
                dt = sdf.format(c.getTime());  // dt is now the new date

                if (dt.compareTo(endDate) > 0) {
                    logger.info("Last date");
                    dt = endDate;
                    done = true;
                }
                logger.info("End Date: {}", dt);

                if (countReplacements >= 2) {
                    statement.setString(2, dt);
                }

                if (countReplacements >= 4) {
                    statement.setString(4, dt);
                }

                try (ResultSet res = statement.executeQuery(false, true); CSVWriter writer = new CSVWriter(new FileWriter(targetFileName, !first), csvSeparator)) {
                    long queryEndTime = System.currentTimeMillis();
                    logger.info("Query completed...writing to CSV. Append: {}. Execution time in seconds: {}", !first, (queryEndTime - queryTimeStart) / 1000);
                    writer.writeAll(res, first);
                    logger.info("Extracted data to file: {}. Time taken in seconds {}", targetFileName, (System.currentTimeMillis() - queryEndTime) / 1000);
                    first = false;
                } catch (SQLException e) {
                    e.printStackTrace();
                    break;
                }
            }

            statement.close();
        } catch (SQLException e) {
            logger.error(e);
        }

        long extractionEndTime = System.currentTimeMillis();
        logger.info("Extraction completed: Total execution time: {}", (extractionEndTime - extractionStartTime) / 1000);
    }

    /**
     * Loads configuration and starts the export process
     *
     * @param prop the configuration properties
     */
    public static void exportData(final Properties prop) {
        String sourceInformixDbHost = prop.getProperty("sourceInformix.dbhost");
        int sourceInformixDbPort = Integer.parseInt(prop.getProperty("sourceInformix.dbport"));
        String sourceInformixDbName = prop.getProperty("sourceInformix.dbname");
        String sourceInformixDbUser = prop.getProperty("sourceInformix.dbuser");
        String sourceInformixDbPass = prop.getProperty("sourceInformix.dbpass");
        String sourceInformixDbServer = prop.getProperty("sourceInformix.dbserver");

        char csvSeparator = prop.getProperty("config.csvSeparator").charAt(0);
        boolean singleOutput = "true".equals(prop.getProperty("config.singleOutput"));
        boolean processInBatch = "true".equals(prop.getProperty("config.processInBatch"));

        String outputFilename = prop.getProperty("config.outputFilename", "out");

        String queriesDirPath = prop.getProperty("config.queriesDir");
        String csvTargetDirectory = prop.getProperty("config.csvTargetDirectory");

        logger.info("queries directory: {}", queriesDirPath);

        File queriesDir = new File(queriesDirPath);
        int i = 0;
        for (final File fileEntry : Objects.requireNonNull(queriesDir.listFiles())) {

//            String targetCsvFileName = outputFilename;
//            if (!singleOutput) {
//                targetCsvFileName = fileEntry.getName() + ".csv";
//            }

            String targetCsvFileName = fileEntry.getName() + ".csv";
            logger.info("Results will be written to file {}", targetCsvFileName);
            logger.info("Connect to database");
            DBInterface sourceInformixDbInterface = new DBInterface(sourceInformixDbName, sourceInformixDbHost, sourceInformixDbServer, sourceInformixDbUser, sourceInformixDbPass, sourceInformixDbPort);

            logger.info("Processing query: {}", fileEntry.getName());
            try {
                String query = readFile(fileEntry.getAbsolutePath(), Charset.defaultCharset());
                logger.info("Query database. Process in batch: {}", processInBatch);
                if (!processInBatch) {
                    InformixDataExport.informixExtractData(sourceInformixDbInterface, query, csvSeparator, csvTargetDirectory + "/" + targetCsvFileName, i == 0 || !singleOutput);
                } else {
                    String startDate = prop.getProperty("config.startDate", "2020-01-01 00:00:00");
                    String endDate = prop.getProperty("config.endDate", tomorrow());
                    int numDaysToIncrementBy = Integer.parseInt(prop.getProperty("config.numDaysToIncrementBy", "5"));
                    InformixDataExport.informixExtractDataInBatch(sourceInformixDbInterface, query, csvSeparator, csvTargetDirectory + "/" + targetCsvFileName, Integer.parseInt(prop.getProperty("config.fetchSize", String.valueOf(Integer.MAX_VALUE))), startDate, endDate, numDaysToIncrementBy);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            i++;
        }
    }

    /**
     * Reads file contents
     *
     * @param path     the file path
     * @param encoding file encoding
     * @return file contents
     * @throws IOException if error occurs
     */
    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Entry method to data extractor
     *
     * @param args input arguments
     * @throws Exception if any error occurs
     */
    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            logger.error("Please provide config.properties file path as parameter");
        } else {
            final long processStartTime = System.currentTimeMillis();
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = Files.newInputStream(Paths.get(args[0]));
                prop.load(input);
                InformixDataExport.exportData(prop);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new Exception("Couldn't find/open properties file " + ex.getMessage());
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            final long processEndTime = System.currentTimeMillis();
            logger.info("Process completed in {} seconds", (processEndTime - processStartTime) / 1000);
        }
    }

    /**
     * Get the next date
     *
     * @return tomorrow's date in the format yyyy-MM-dd HH:mm:00
     */
    private static String tomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);  // number of days to add

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

        return sdf.format(c.getTime());
    }
}
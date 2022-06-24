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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles data export from database
 *
 * @author TCSASSEMBLER
 */
public class InformixDataExport {
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
    public static void informixExtractData(final DBInterface informixDbInterface, final String query,
                                           final char csvSeparator, final String targetFileName, boolean includeColumns) throws Exception {
        try (InformixDBConnect informixDBConnect = new InformixDBConnect(informixDbInterface);
             Connection conn = informixDBConnect.getNewConnection(); Statement stmt = conn.createStatement()) {
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

    public static void informixExtractDataInBatch(final DBInterface informixDBInterface, String query, final char csvSeparator, final String targetFileName, boolean includeColumns) throws Exception {

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

        File queriesDir = new File(queriesDirPath);
        int i = 0;
        for (final File fileEntry : Objects.requireNonNull(queriesDir.listFiles())) {

            String targetCsvFileName = outputFilename;
            if (!singleOutput) {
                targetCsvFileName = fileEntry.getName() + ".csv";
            }

            logger.info("Processing query: {}", fileEntry.getName());

            DBInterface sourceInformixDbInterface = new DBInterface(
                    sourceInformixDbName, sourceInformixDbHost,
                    sourceInformixDbServer, sourceInformixDbUser, sourceInformixDbPass, sourceInformixDbPort
            );

            try {
                String query = readFile(fileEntry.getAbsolutePath(), Charset.defaultCharset());
                if (!processInBatch) {
                    InformixDataExport.informixExtractData(sourceInformixDbInterface, query, csvSeparator, csvTargetDirectory + "/" + targetCsvFileName, i == 0 || !singleOutput);
                } else {
                    InformixDataExport.informixExtractDataInBatch(sourceInformixDbInterface, query, csvSeparator, csvTargetDirectory + "/" + targetCsvFileName, i == 0 || !singleOutput);
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
    private static String readFile(String path, Charset encoding)
            throws IOException {
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
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(args[0]);
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
        }
    }
}
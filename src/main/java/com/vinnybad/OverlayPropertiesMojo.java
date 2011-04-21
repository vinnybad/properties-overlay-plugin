package com.vinnybad;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which overlays a set of properties files in a folder with a default set
 * of properties.
 * 
 * @goal overlayProperties
 * 
 * @phase process-resources
 */
public class OverlayPropertiesMojo extends AbstractMojo {

	private static final String PROPERTIES_FILE_REGEX = "^(?!default).*?\\.properties$";

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 */
	private File outputDirectory;

	/**
	 * Source directory with location of the properties files
	 * 
	 * @parameter default-value="conf"
	 */
	private File propertiesSourceDirectory;

	/**
	 * Default properties file. This is what will be the base set of properties.
	 * 
	 * @parameter default-value="conf/default.properties"
	 */
	private File defaultPropertiesFile;

	public void execute() throws MojoExecutionException {
		boolean canMoveForward = fileExistsCheck( propertiesSourceDirectory, "defaultPropertiesFile" );
		canMoveForward &= fileExistsCheck( defaultPropertiesFile, "defaultPropertiesFile" );
		outputDirectory.mkdirs();
		
		if( !canMoveForward ) {
			System.out.println( "Please fix the above errors..." );
			System.exit( -1 );
		} else {
			for( File overridePropertyFile : findPropertyFiles() ) {
				try {
					System.out.print( "Processing " + overridePropertyFile.getName() + "..." );
					Writer outputFile = new FileWriter( outputDirectory.getPath() + File.separator + overridePropertyFile.getName() );
					Properties defaultProps = overlayPropertiesFiles( defaultPropertiesFile, overridePropertyFile);
					defaultProps.store( outputFile, "Properties were overlayed at " + new Date() );
					System.out.println( "done!" );
				} catch (Exception ex) {
					System.err.println( ex.getMessage() + ex.toString() );
				}
			}
		}
	}

	private File[] findPropertyFiles() {
		File[] propertyFiles = propertiesSourceDirectory.listFiles( new FilenameFilter() {
			@Override
			public boolean accept(File parentDirectory, String fileName) {
				return Pattern.matches( PROPERTIES_FILE_REGEX, fileName );
			}
		} );
		return propertyFiles;
	}

	private Properties overlayPropertiesFiles( File basePropertyFile, File overridePropertyFile) throws IOException,
			FileNotFoundException {
		Properties defaultProps = loadPropertiesFromFile( basePropertyFile );
		Properties overrideProps = loadPropertiesFromFile( overridePropertyFile );
		defaultProps.putAll( overrideProps );
		return defaultProps;
	}

	private Properties loadPropertiesFromFile( File propertiesFile ) throws IOException, FileNotFoundException {
		Properties defaultProps = new Properties();
		defaultProps.load( new FileInputStream( propertiesFile ) );
		return defaultProps;
	}

	private boolean fileExistsCheck( File theRequiredFile, String propertyName ) {
		boolean fileExists = theRequiredFile.exists();
		if ( !fileExists ) {
			System.out.println( String.format( propertyName + "is invalid: did not find the element " + theRequiredFile.getAbsolutePath() ) );
		}
		
		return fileExists;
	}
}

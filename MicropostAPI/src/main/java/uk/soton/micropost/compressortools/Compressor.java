package uk.soton.micropost.compressortools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import twitter4j.Logger;

public class Compressor {
	private File indir;
	private File outdir;





	public Compressor(File indir, File outdir) {
		this.indir=indir;
		this.outdir=outdir;
	}
	public Compressor(File indir) {
		this(indir, indir);
	}
	public static void main(String[] args) {
		String folders[]=new String[]{
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/elections",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/brexit",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/lightpollution",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/voteremain",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/voteleave",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/votestay",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/britainout",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/posbrexit",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/negbrexit",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/negusa",
				"/media/zerr/BA0E0E3E0E0DF3E3/tweets/posusa",
				
				
				
		};
for(String fname:folders){		
Compressor c=new Compressor(new File(fname),
		new File(fname+"zip"));
c.compress();
}
	}
	
	public void compress()
	{
		log().info("get file list");
		List<File> tocompress=Arrays.asList(indir.listFiles());
		log().info("file list of "+tocompress.size()+"elements ok, sort it");
		Hashtable<File, FileTime> cache=new Hashtable<>();
		Collections.sort(tocompress, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
			
				 try {
					 FileTime attr1=	cache.get(o1);
							 
						if(attr1==null){	 
							cache.put(o1,attr1=Files.readAttributes(Paths.get(o1.toURI()) , BasicFileAttributes.class).creationTime());
							
						}
						
						 FileTime attr2=	cache.get(o2);
						 
							if(attr2==null){	 
								cache.put(o2,attr2=Files.readAttributes(Paths.get(o1.toURI()) , BasicFileAttributes.class).creationTime());
								
							}
					
					return attr1.compareTo(attr2);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return Long.compare(o1.lastModified(), o2.lastModified());
			}
		});
		log().info("sorted, compress it");
		int maxcnt=500;
	
		for(int idx=0;idx<tocompress.size();idx+=maxcnt)
		{
			int toidx=Math.min(idx+maxcnt, tocompress.size());
			try {
				List<File> cursublist = tocompress.subList(idx, toidx);
				String outfile = cursublist.get(0).getName()+"_"+(idx+"-"+toidx)+".tar.gz";
				log().info("sorted, compress it "+outfile);
				compressFiles(cursublist,new File(outdir,outfile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
		
		
	}

	public static void compressFile(File file, File output)

	throws IOException

	{

		ArrayList<File> list = new ArrayList<File>(1);

		list.add(file);

		compressFiles(list, output);

	}

	/**
	 * 21 Compress (tar.gz) the input files to the output file 22
	 *
	 * 23
	 * 
	 * @param files
	 *            The files to compress 24
	 * @param output
	 *            The resulting output file (should end in .tar.gz) 25
	 * @throws IOException
	 *             26
	 */

	public static void compressFiles(Collection<File> files, File output)

	throws IOException

	{

		log().debug("Compressing " + files.size() + " to " + output.getAbsoluteFile());

		// Create the output stream for the output file

		FileOutputStream fos = new FileOutputStream(output);

		// Wrap the output file stream in streams that will tar and gzip
		// everything

		TarArchiveOutputStream taos = new TarArchiveOutputStream(

		new GZIPOutputStream(new BufferedOutputStream(fos)));

		// TAR has an 8 gig file limit by default, this gets around that

		//taos.setBigNumberMode(TarArchiveOutputStream.LONGFILE_GNU); // to get
																		// past
																		// the 8
																		// gig
																		// limit

		// TAR originally didn't support long file names, so enable the support
		// for it

		taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

		// Get to putting all the files in the compressed output file

		for (File f : files) {

			addFilesToCompression(taos, f, ".");

		}

		// Close everything up

		taos.close();

		fos.close();
	}
		/**
		52
		 * Does the work of compression and going recursive for nested directories
		53
		 * <p/>
		54
		 *
		55
		 * Borrowed heavily from http://www.thoughtspark.org/node/53
		56
		 *
		57
		 * @param taos The archive
		58
		 * @param file The file to add to the archive
		59
		     * @param dir The directory that should serve as the parent directory in the archivew
		60
		 * @throws IOException
		61
		 */
	
		private static void addFilesToCompression(TarArchiveOutputStream taos, File file, String dir)
	
		    throws IOException
		
		{
		
		            // Create an entry for the file
		
		    taos.putArchiveEntry(new TarArchiveEntry(file, new File(dir,file.getName()).toString()));
		
		    if (file.isFile()) {
		
		                    // Add the file to the archive
		
		        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		
		        IOUtils.copy(bis, taos);
		
		        taos.closeArchiveEntry();
		
		        bis.close();
		
		    }
		
		    else if (file.isDirectory()) {
		
		                    // close the archive entry
		
		        taos.closeArchiveEntry();
		
		                    // go through all the files in the directory and using recursion, add them to the archive
		
		        for (File childFile : file.listFiles()) {
		
		            addFilesToCompression(taos, childFile, new File(dir,file.getName()).toString());
		
		        }
		
		    }
		
		}



	

	private static  Logger log() {
		// TODO Auto-generated method stub
		return Logger.getLogger(Compressor.class);
	}

}

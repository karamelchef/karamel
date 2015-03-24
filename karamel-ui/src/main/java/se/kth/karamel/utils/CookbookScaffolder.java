package se.kth.karamel.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.karamel.common.ClasspathResourceUtil;
import se.kth.karamel.common.Settings;
import se.kth.karamel.webservice.KaramelServiceApplication;

public class CookbookScaffolder {
    
    public static boolean mkdirs(String path) {
        File cbDir = new File(path);
        return cbDir.mkdirs();
    }

    public static boolean mkFile(String path, StringBuffer contents) throws IOException {
        File f = new File(path);
        return f.createNewFile();
    }

    public static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) {
            throw new FileNotFoundException(path.getAbsolutePath());
        }
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
    
    /**
     * 
     * @param path file to be created
     * @param template from resources - /
     * @param name
     * @throws IOException 
     */
    public static void createFile(String path, String template, String name) throws IOException {
            String script = ClasspathResourceUtil.readContent(template);
            script = script.replaceAll("%%NAME%%", name);
            File f = new File(path);
            // write contents to file as text, not binary data
            PrintWriter out = new PrintWriter(path);
            out.println(script);
            out.flush();
    }

    public static void create() {
        String name="";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter cookbook name: ");
            name = br.readLine();
            File cb = new File("cookbooks" + File.separator + name);
            if (cb.exists()) {
                boolean wiped = false;
                while (!wiped) {
                    System.out.print("Do you want to over-write the existing cookbook " + name + "? (y/n) ");
                    String overwrite = br.readLine();
                    if (overwrite.compareToIgnoreCase("n") == 0 || overwrite.compareToIgnoreCase("no") == 0) {
                        System.out.println("Not over-writing. Exiting.");
                        System.exit(0);
                    }
                    if (overwrite.compareToIgnoreCase("y") == 0 || overwrite.compareToIgnoreCase("yes") == 0) {
                        deleteRecursive(cb);
                        wiped=true;
                    }
                }
            }
            
            String cbName = "cookbooks" + File.separator + name + File.separator;
            
            // Create all the directories for the coookbook 
            mkdirs(cbName + "recipes");
            mkdirs(cbName + "attributes");
            mkdirs(cbName + "templates" + File.separator + "default");

//            String uid = System.getProperty("user.name"); 

            // Create all the files for the coookbook using the file-templates in the resources
            createFile(cbName + Settings.COOKBOOK_DEFAULTRB_REL_PATH, Settings.CB_TEMPLATE_ATTRIBUTES_DEFAULT, name);
            createFile(cbName + Settings.COOKBOOK_BERKSFILE_REL_PATH, Settings.CB_TEMPLATE_BERKSFILE, name);
            createFile(cbName + Settings.COOKBOOK_METADATARB_REL_PATH, Settings.CB_TEMPLATE_METADATA, name);
            createFile(cbName + Settings.COOKBOOK_KARAMELFILE_REL_PATH, Settings.CB_TEMPLATE_KARAMELFILE, name);
            createFile(cbName + Settings.COOKBOOK_RECIPE_INSTALL_PATH, Settings.CB_TEMPLATE_RECIPE_INSTALL, name);
            createFile(cbName + Settings.COOKBOOK_RECIPE_DEFAULT_PATH, Settings.CB_TEMPLATE_RECIPE_DEFAULT, name);            
            createFile(cbName + Settings.COOKBOOK_RECIPE_MASTER_PATH, Settings.CB_TEMPLATE_RECIPE_MASTER, name);
            createFile(cbName + Settings.COOKBOOK_RECIPE_SLAVE_PATH, Settings.CB_TEMPLATE_RECIPE_SLAVE, name);            
            createFile(cbName + Settings.COOKBOOK_KITCHEN_YML_PATH, cbName + Settings.CB_TEMPLATE_KITCHEN_YML, name);            
            createFile(cbName + Settings.COOKBOOK_CONFIG_FILE_PATH, cbName + Settings.CB_TEMPLATE_CONFIG_PROPS, name);            
            createFile(cbName + Settings.COOKBOOK_MASTER_SH_PATH, cbName + Settings.CB_TEMPLATE_MASTER_SH, name);            
            createFile(cbName + Settings.COOKBOOK_SLAVE_SH_PATH, cbName + Settings.CB_TEMPLATE_SLAVE_SH, name);            
            System.out.println("Cookbook scaffolding for " + name);
            System.out.println("Cookbook now in folder: ./cookbooks/" + name);
                        
        } catch (IOException ex) {
            Logger.getLogger(KaramelServiceApplication.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
            System.exit(0);
        }
    }

    static public String readFile(String path)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }
    
    
}

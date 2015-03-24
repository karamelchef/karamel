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
    }

    public static void create() {
        String name="";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter cookbook name:");
            name = br.readLine();
            File cb = new File("cookbooks" + File.separator + name);
            if (cb.exists()) {
                boolean wiped = false;
                while (!wiped) {
                    System.out.print("Do you want to over-write the existing cookbook " + name + "? (y/n)");
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
            mkdirs("cookbooks" + File.separator + name + File.separator + "recipes");
            mkdirs("cookbooks" + File.separator + name + File.separator + "attributes");
            mkdirs("cookbooks" + File.separator + name + File.separator + "templates" + File.separator + "default");

//            String uid = System.getProperty("user.name"); 

            String defaultAttrs = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_DEFAULTRB_REL_PATH;
            createFile(defaultAttrs, Settings.CB_TEMPLATE_ATTRIBUTES_DEFAULT, name);
            String berks = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_BERKSFILE_REL_PATH;
            createFile(berks, Settings.CB_TEMPLATE_BERKSFILE, name);
            String metadata = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_METADATARB_REL_PATH;
            createFile(metadata, Settings.CB_TEMPLATE_METADATA, name);
            String karamel = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_KARAMELFILE_REL_PATH;
            createFile(karamel, Settings.CB_TEMPLATE_KARAMELFILE, name);
            String installRecipe = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_RECIPE_INSTALL_PATH;
            createFile(installRecipe, Settings.CB_TEMPLATE_RECIPE_INSTALL, name);
            String defaultRecipe = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_RECIPE_DEFAULT_PATH;
            createFile(defaultRecipe, Settings.CB_TEMPLATE_RECIPE_DEFAULT, name);            
            String masterRecipe = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_RECIPE_MASTER_PATH;
            createFile(masterRecipe, Settings.CB_TEMPLATE_RECIPE_MASTER, name);
            String slaveRecipe = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_RECIPE_SLAVE_PATH;
            createFile(slaveRecipe, Settings.CB_TEMPLATE_RECIPE_SLAVE, name);            
            String kitchen = "cookbooks" + File.separator + name + File.separator + Settings.COOKBOOK_KITCHEN_YML_PATH;
            createFile(kitchen, Settings.CB_TEMPLATE_KITCHEN_YML, name);            
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

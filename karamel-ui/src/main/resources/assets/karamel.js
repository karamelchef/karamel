
// =========== HELPER METHODS ============= //

function getClusterHelperMethods() {

    return {

        // helper method to check if the cookbook array contains the cookbook.
        containsCookbook: function (cookbookArray, cookbook) {

            // NULL Check.
            if (cookbookArray == null || cookbook == null) {
                return false;
            }

            for (var i = 0; i < cookbookArray.length; i++) {
                if (cookbook.name === cookbookArray[i].name && cookbook.github == cookbookArray, github) {
                    return true;
                }
            }

            return false;
        },

        loadGroups: function (container, groups) {
            for (var i = 0; i < groups.length; i++) {

                var group = new NodeGroup();
                group.load(groups[i]);

                // ===== Add Cookbooks to the group.
                var cookbooks = groups[i]["cookbooks"];
                this.loadCookbooks(group, cookbooks);

                container.addNodeGroup(group);
            }
        },

        loadCookbooks : function(container, cookbooks){

            for (var i = 0; i < cookbooks.length; i++) {
                var cookbook = new Cookbook();
                cookbook.load(cookbooks[i]);
                container.addCookbook(cookbook);

                var recipes = cookbooks[i]["recipes"];

                // ===== Add Recipes to the group.
                for (var j = 0; j < recipes.length; j++) {
                    cookbook.addRecipe(new Recipe(recipes[j]["name"]));
                }
            }
        },

        copyGroups : function(container, groups){
            for (var i = 0; i < groups.length; i++) {

                var group = new NodeGroup();
                group.copy(groups[i]);

                // ===== Add Cookbooks to the group.
                var cookbooks = groups[i]["cookbooks"];
                this.copyCookbooks(group, cookbooks);

                container.addNodeGroup(group);
            }
        },

        copyCookbooks : function(container, cookbooks){

            for (var i = 0; i < cookbooks.length; i++) {
                var cookbook = new Cookbook();
                cookbook.copy(cookbooks[i]);
                container.addCookbook(cookbook);

                var recipes = cookbooks[i]["recipes"];

                // ===== Add Recipes to the group.
                for (var j = 0; j < recipes.length; j++) {
                    cookbook.addRecipe(new Recipe(recipes[j]["title"]));
                }
            }
        }
    }
}

// ================================================  CLUSTER ======================================  //
function Cluster() {

    this.name = null;
    this.cookbooks = [];
    this.nodeGroups = [];

    // Externally add the providers.
    this.ec2Provider = null;
    this.vagrantProvider = {};
    this.helperObj = getClusterHelperMethods();


    this.setEC2Provider = function (ec2Provider) {
        this.ec2Provider = ec2Provider;
    };

    this.getEC2provider = function () {
        return this.ec2Provider;
    };


    // Number of Node groups.
    this.numberOfNodeGroups = function () {
        return this.nodeGroups.length;
    };

    // Add a cookbook.
    this.addCookbook = function (cookbook) {
        if (this.cookbooks == null) {
            this.cookbooks = [];
        }
        this.cookbooks.push(cookbook);
    };

    // Remove an existing cookbook.
    this.removeCookbook = function (cookbook) {

        var id = -1;
        // In this we can also override the equals method in the cookbook object and then call the equals method TODO :==============
        for (var i = 0; i < this.cookbooks.length; i++) {
            if (this.cookbooks[i].name === cookbook.name) {
                id = i;
            }
        }

        // If any match found, then remove the entry.
        if (id != -1) {
            this.cookbooks.splice(id, 1);
        }
    };

    this.getCookbooks = function () {
        return this.cookbooks;
    };

    this.setCookbooks = function (cookbooksArray) {
        this.cookbooks = cookbooksArray;
    };

    this.containsCookbook = function (cookbook) {
        for (var i = 0; i < this.cookbooks.length; i++) {
            if (cookbook.name === this.cookbooks[i].name && cookbook.github === this.cookbooks[i].github) {
                return this.cookbooks[i];
            }
        }
        return null;
    };

    // add a new node group to the cluster.
    this.addNodeGroup = function (group) {
        this.nodeGroups.push(group);
    };

    this.copyUpdatedClusterData = function (updatedClusterInfo) {

        this.name = updatedClusterInfo.name;
        this.cookbooks = updatedClusterInfo.cookbooks;
        this.nodeGroups = updatedClusterInfo.nodeGroups;
    };


    // ======== Load Data from an external object.
    this.load = function (other) {

        this.name = other.name;
        this.helperObj.loadGroups(this, other["groups"]);
        this.helperObj.loadCookbooks(this, other["cookbooks"]);
    };


    // Copy the cluster obj.
    this.copy = function (other) {

        this.name = other.name;
        if(other.ec2Provider != null){
            this.ec2Provider = new EC2Provider();
            this.ec2Provider.load(other.ec2Provider);
        }
        this.helperObj.copyGroups(this,other["nodeGroups"]);
        this.helperObj.copyCookbooks(this,other["cookbooks"]);
    };

}


// ============================================  PROVIDERS ============================================ //
function Provider(name) {

}

function EC2Provider() {

    this.accountId = null;
    this.accountKey = null;
    this.pubKey = null;

    this.load = function (other) {
        this.accountId = other.accountId;
        this.accountKey = other.accountKey;
        this.pubKey = other.pubKey;
    }
}

function VagrantProvider(name, ip) {
    this.name = name;
    this.ip = ip;
}


// ===========================================  COOKBOOKS ============================================== //
function Cookbook() {
    this.name = null;
    this.github = null;
    this.branch = null;
    this.version = null;
    this.attributes = {};
    this.cookbookHomeUrl = null;
    this.recipes = [];

    this.addPropertyToAttributes = function (key, value) {
        this.attributes[key] = value;
    };

    this.removePropertyFromAttributes = function (key) {
        delete this.attributes[key];
    };

    this.equals = function (other) {
        if (other != null) {
            if (this.github === other.github && this.cookbookHomeUrl === other.cookbookHomeUrl) {
                return true;
            }
        }
        return false;
    };

    // Load data into the cookbook.
    this.load = function (other) {
        this.name = other.name;
        this.github = other.github;
        this.branch = other.branch;
        this.version = other.version;
        this.attributes = other.attrs;          // FIX ME: Name discrepancy should not be there.
        this.cookbookHomeUrl = other.cookbookHomeUrl;
    };


    this.copy = function (other) {
        this.name = other.name;
        this.github = other.github;
        this.branch = other.branch;
        this.version = other.version;
        this.attributes = other.attributes;
        this.cookbookHomeUrl = other.cookbookHomeUrl;

        // Load recipes instead of copying.
    };

    // Add recipe to the cookbook.
    this.addRecipe = function (recipe) {
        if (this.recipes == null) {
            this.recipes = [];
        }
        this.recipes.push(recipe);

    };

    this.containsRecipe = function (recipe) {

        if (this.recipes == null) {
            return false;
        }

        for (var i = 0; i < this.recipes.length; i++) {
            if (recipe.title === this.recipes[i].title) {
                console.log(" Inside Comparison." + " Received: " + recipe.title + " Present: " + this.recipes[i].title);
                return true;
            }
        }
        return false;
    };


    this.removeRecipe = function (receipe) {
        var id = -1;
        for (var i = 0; i < this.recipes.length; i++) {
            if (this.recipes[i].title == receipe.title) {
                id = i;
                break;
            }
        }

        // If any recipe found.
        if (id != -1) {
            this.recipes.splice(id, 1);
        }
    };
}

// ==============================================================  GROUP  ===============================  //
function NodeGroup() {

    this.name = "";
    this.provider = "";
    this.attrs = [];
    this.instances = 0;
    this.ec2 = {};
    this.vagrant = {};
    this.cookbooks = [];

    this.setEC2Provider = function (ec2Provider) {
        this.ec2 = ec2Provider;
    };

    this.getEC2provider = function () {
        return this.ec2;
    };

    this.getVagrantProvider = function () {
        return this.vagrant;
    };

    this.setVagrantProvider = function (vagrantProvider) {
        this.vagrant = vagrantProvider;
    };

    this.addCookbook = function (cookbook) {
        this.cookbooks.push(cookbook);
    };

    this.getCookbooks = function () {
        return this.cookbooks;
    };

    this.setCookbooks = function (cookbooksArray) {
        this.cookbooks = cookbooksArray;
    };

    // Load the variables from this object.
    this.load = function (group) {
        this.name = group.name;
        this.instances = group.size;
        this.ec2 = group.ec2;
        this.vagrant = group.vagrant;
    };

    // Copy the data from a similar instance of node group.
    this.copy = function (other) {

        this.name = other.name;
        this.provider = other.provider;
        this.attrs = other.attrs;
        this.instances = other.instances;
        this.ec2 = other.ec2;
        this.vagrant = other.vagrant;

        // Load cookbooks instead of copying.
    };

    // ==== Is Similar Cookbook Present ?
    this.containsCookbook = function (cookbook) {
        for (var i = 0; i < this.cookbooks.length; i++) {
            if (cookbook.name === this.cookbooks[i].name && cookbook.github === this.cookbooks[i].github) {
                return this.cookbooks[i];
            }
        }
        return null;
    };

    this.removeCookbook = function (cookbook) {
        var index = -1;

        for (var i = 0; i < this.cookbooks.length; i++) {
            if (this.cookbooks[i].equals(cookbook)) {
                index = i;
            }
        }

        if (index != -1) {
            this.cookbooks.splice(index);
        }
    }

}

// ====================================================  RECIPE ==================================  //
function Recipe(title) {
    this.title = title;
}


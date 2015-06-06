// ================================================  CLUSTER ======================================  //
function Cluster() {
  this.name = null;
  this.cookbooks = [];
  this.groups = [];
  this.ec2 = null;
  this.baremetal = null;
  this.sshKeyPair = null;

  this.addCookbook = function(cookbook) {
    if (this.cookbooks == null) {
      this.cookbooks = [];
    }
    this.cookbooks.push(cookbook);
  };

  this.removeCookbook = function(cookbook) {
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

  this.containsCookbook = function(cookbook) {
    for (var i = 0; i < this.cookbooks.length; i++) {
      if (cookbook.name === this.cookbooks[i].name && cookbook.github === this.cookbooks[i].github) {
        return this.cookbooks[i];
      }
    }
    return null;
  };

  this.addGroup = function(group) {
    this.groups.push(group);
  };

  this.copyUpdatedClusterData = function(updatedClusterInfo) {
    this.name = updatedClusterInfo.name;
    this.cookbooks = updatedClusterInfo.cookbooks;
    this.groups = updatedClusterInfo.groups;
  };


  this.load = function(other) {
    this.name = other.name;
    this.loadEc2(this, other["ec2"]);
    this.loadBaremetal(this, other["baremetal"]);
    this.loadSshKeyPair(this, null);
    this.loadGroups(this, other["groups"]);
    this.loadCookbooks(this, other["cookbooks"]);
  };

  this.copy = function(other) {
    this.name = other.name;
    if (other.ec2 !== null) {
      this.ec2 = new Ec2();
      this.ec2.copy(other.ec2);
    } else
      this.ec2 = null;

    if (other.baremetal !== null) {
      this.baremetal = new Baremetal();
      this.baremetal.copy(other.baremetal);
    } else
      this.baremetal = null;

    if (other.sshKeyPair !== null) {
      this.sshKeyPair = new SshKeyPair();
      this.sshKeyPair.copy(other.sshKeyPair);
    }
    this.copyGroups(this, other["groups"]);
    this.copyCookbooks(this, other["cookbooks"]);
  };

  this.containsCookbook = function(cookbookArray, cookbook) {
    if (cookbookArray == null || cookbook == null) {
      return false;
    }

    for (var i = 0; i < cookbookArray.length; i++) {
      if (cookbook.name === cookbookArray[i].name && cookbook.github == cookbookArray, github) {
        return true;
      }
    }
    return false;
  };

  this.loadGroups = function(container, groups) {
    for (var i = 0; i < groups.length; i++) {
      var group = new Group();
      group.load(groups[i]);
      var cookbooks = groups[i]["cookbooks"];
      this.loadCookbooks(group, cookbooks);
      container.addGroup(group);
    }
  };

  this.loadEc2 = function(container, provider) {
    if (!(_.isUndefined(provider) || _.isNull(provider))) {
      var ec2 = new Ec2();
      ec2.load(provider);
      container.ec2 = ec2;
    } else
      container.ec2 = null;
  };

  this.loadBaremetal = function(container, provider) {
    if (!(_.isUndefined(provider) || _.isNull(provider))) {
      var baremetal = new Baremetal();
      baremetal.load(provider);
      container.baremetal = baremetal;
    } else
      container.baremetal = null;
  };

  this.loadSshKeyPair = function(container, sshKeyPair) {
    container.sshKeyPair = new SshKeyPair();
  };

  this.loadCookbooks = function(container, cookbooks) {
    for (var i = 0; i < cookbooks.length; i++) {
      var cookbook = new Cookbook();
      cookbook.load(cookbooks[i]);
      container.addCookbook(cookbook);
      var recipes = cookbooks[i]["recipes"];
      for (var j = 0; j < recipes.length; j++) {
        cookbook.addRecipe(new Recipe(recipes[j]["name"]));
      }
    }
  };

  this.copyGroups = function(container, groups) {
    for (var i = 0; i < groups.length; i++) {
      var group = new Group();
      group.copy(groups[i]);
      var cookbooks = groups[i]["cookbooks"];
      this.copyCookbooks(group, cookbooks);
      container.addGroup(group);
    }
  };

  this.copyCookbooks = function(container, cookbooks) {
    for (var i = 0; i < cookbooks.length; i++) {
      var cookbook = new Cookbook();
      cookbook.copy(cookbooks[i]);
      container.addCookbook(cookbook);
      var recipes = cookbooks[i]["recipes"];
      for (var j = 0; j < recipes.length; j++) {
        cookbook.addRecipe(new Recipe(recipes[j]["title"]));
      }
    }
  };

  this.hasEc2 = function() {
    if (this.ec2)
      return true;
    else {
      for (var i = 0; i < this.cookbooks; i++) {
        if (cookbooks[i].ec2)
          return true;
      }
    }
    return false;
  };

  this.hasBaremetal = function() {
    if (this.baremetal)
      return true;
    else {
      for (var i = 0; i < this.cookbooks; i++) {
        if (cookbooks[i].baremetal)
          return true;
      }
    }
    return false;
  };

  this.areCredentialsSet = function() {
    var result = true;
    if (this.hasEc2()) {
      result = result && this.ec2.getIsValid();
    }
    result = result && this.sshKeyPair.getIsValid();
    return result;
  };
  
  this.toCoreApiFormat = function() {
    var coreFormatCluster = toCoreApiFormat(this);
    return coreFormatCluster;
  };
}

function Credentials() {
}

Credentials.prototype = {
  mapKey: "",
  isValid: false,
  setIsValid: function(isValid) {
    this.isValid = isValid;
  },
  getIsValid: function() {
    return this.isValid;
  },
  getMapKey: function() {
    return this.mapKey;
  }
};



// ============================================  SSH KEYS ============================================ //
function SshKeyPair() {
  this.mapKey = "ssh";
  this.pubKey = null;
  this.priKey = null;
  this.pubKeyPath = null;
  this.privKeyPath = null;
  this.passphrase = null;

  this.copy = function(other) {
    this.isValid = other.isValid;
  }
}

SshKeyPair.prototype = Object.create(Credentials.prototype);

// ============================================  PROVIDERS ============================================ //
function Provider(name) {

}

Provider.prototype = Object.create(Credentials.prototype);
Provider.prototype.isActive = false;

function Ec2() {
  this.mapKey = "ec2";
  this.type = null;
  this.ami = null;
  this.region = null;
  this.price = null;
  this.vpc = null;
  this.subnet = null;
  this.accessKey = null;
  this.secretKey = null;

  this.load = function(other) {
    this.type = other.type || null;
    this.ami = other.ami || null;
    this.region = other.region || null;
    this.price = other.price || null;
    this.vpc = other.vpc || null;
    this.subnet = other.subnet || null;
  };

  this.copy = function(other) {
    this.type = other.type || null;
    this.ami = other.ami || null;
    this.region = other.region || null;
    this.price = other.price || null;
    this.vpc = other.vpc || null;
    this.subnet = other.subnet || null;
    this.accessKey = other.accessKey || null;
    this.secretKey = other.secretKey || null;
  };

  this.addAccountDetails = function(other) {
    this.accessKey = other.accessKey || null;
    this.secretKey = other.secretKey || null;
  };
}

// Inherit from the Provider.
Ec2.prototype = Object.create(Provider.prototype);

function Baremetal() {
  this.mapKey = "baremetal";
  this.username = null;
  this.ips = [];
  this.load = function(other) {
    this.username = other.username || null;
    this.ips = other.ips || null;
  };
  this.copy = function(other) {
    this.username = other.username || null;
    if (typeof (other.ips) === 'string' || other.ips instanceof String)
      this.ips = other.ips.split("\n");
    else
      this.ips = other.ips || null;
  };
}

Baremetal.prototype = Object.create(Provider.prototype);

// ===========================================  COOKBOOKS ============================================== //
function Cookbook() {
  this.name = null;
  this.github = null;
  this.branch = null;
  this.version = null;
  this.attributes = {};
  this.cookbookHomeUrl = null;
  this.recipes = [];

  this.addPropertyToAttributes = function(key, value) {
    this.attributes[key] = value;
  };

  this.removePropertyFromAttributes = function(key) {
    delete this.attributes[key];
  };

  this.equals = function(other) {
    if (other != null) {
      if (this.github === other.github && this.cookbookHomeUrl === other.cookbookHomeUrl) {
        return true;
      }
    }
    return false;
  };

  // Load data into the cookbook.
  this.load = function(other) {
    this.name = other.name;
    this.github = other.github;
    this.branch = other.branch;
    this.version = other.version;
    this.attributes = other.attrs;          // FIX ME: Name discrepancy should not be there.
    this.cookbookHomeUrl = other.cookbookHomeUrl;
  };


  this.copy = function(other) {
    this.name = other.name;
    this.github = other.github;
    this.branch = other.branch;
    this.version = other.version;
    this.attributes = other.attributes;
    this.cookbookHomeUrl = other.cookbookHomeUrl;

    // Load recipes instead of copying.
  };

  // Add recipe to the cookbook.
  this.addRecipe = function(recipe) {
    if (this.recipes == null) {
      this.recipes = [];
    }
    this.recipes.push(recipe);

  };

  this.containsRecipe = function(recipe) {

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


  this.removeRecipe = function(receipe) {
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
function Group() {

  this.name = "";
  this.provider = "";
  this.attrs = [];
  this.size = 0;
  this.ec2 = {};
  this.baremetal = {};
  this.cookbooks = [];

  this.addCookbook = function(cookbook) {
    this.cookbooks.push(cookbook);
  };

  // Load the variables from this object.
  this.load = function(group) {
    this.name = group.name;
    this.size = group.size;
    this.ec2 = group.ec2;
    this.baremetal = group.baremetal;
  };

  // Copy the data from a similar instance of node group.
  this.copy = function(other) {

    this.name = other.name;
    this.provider = other.provider;
    this.attrs = other.attrs;
    this.size = other.size;
    this.ec2 = other.ec2;
    this.baremetal = other.baremetal;

    // Load cookbooks instead of copying.
  };

  // ==== Is Similar Cookbook Present ?
  this.containsCookbook = function(cookbook) {
    for (var i = 0; i < this.cookbooks.length; i++) {
      if (cookbook.name === this.cookbooks[i].name && cookbook.github === this.cookbooks[i].github) {
        return this.cookbooks[i];
      }
    }
    return null;
  };

  this.removeCookbook = function(cookbook) {
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

// ** ====== REST BUILDER METHOD . =========== **//

function toCoreApiFormat(uiCluster) {
  function _addBaremetal(container, provider) {
        if (provider) {
    var baremetal = new _Baremetal();
    baremetal.load(provider);
    container.baremetal = baremetal;
        }
  }
  function _addEc2(container, provider) {
    if (provider) {
    var ec2 = new _Ec2();
    ec2.load(provider);
    container.ec2 = ec2;
    }
  }

  function _addCookbooks(container, cookbooks) {
    for (var i = 0; i < cookbooks.length; i++) {
      var cookbook = new _Cookbook();
      cookbook.load(cookbooks[i]);
      container.addCookbook(cookbook);
    }
  }

  function _addGroups(container, groups) {
    for (var i = 0; i < groups.length; i++) {
      var group = new _Group();
      group.load(groups[i]);
      container.addGroup(group);
    }
  }

  function _addRecipes(container, recipes) {
    for (var i = 0; i < recipes.length; i++) {
      var recipe = new _Recipe();
      recipe.load(recipes[i]);
      container.addRecipe(recipe);
    }
  }

  function _Cluster() {

    this.name = null;
    this.cookbooks = null;
    this.groups = null;
    this.ec2 = null;
    this.baremetal = null;

    this.addCookbook = function(cookbook) {
      if (this.cookbooks === null) {
        this.cookbooks = [];
      }
      this.cookbooks.push(cookbook);
    };

    this.addGroup = function(group) {
      if (this.groups == null) {
        this.groups = [];
      }
      this.groups.push(group);
    };

    this.load = function(other) {
      this.name = other.name;
      _addEc2(this, other.ec2);
      _addBaremetal(this, other.baremetal);
      _addCookbooks(this, other.cookbooks);
      _addGroups(this, other.groups);
    }

  }


  function _Group() {
    this.name = null;
    this.cookbooks = null;
    this.size = null;
    this.ec2 = null;
    this.baremetal = null;

    this.addCookbook = function(cookbook) {
      if (this.cookbooks == null) {
        this.cookbooks = [];
      }
      this.cookbooks.push(cookbook);
    };

    this.load = function(other) {
      this.name = other.name;
      this.size = other.size;
      _addCookbooks(this, other.cookbooks);
      _addEc2(this, other.ec2);
      _addBaremetal(this, other.baremetal);
    }
  }

  function _Cookbook() {
    this.name = null;
    this.attrs = null;
    this.branch = null;
    this.version = null;
    this.cookbookHomeUrl = null;
    this.github = null;

    this.addRecipe = function(recipe) {
      if (this.recipes == null) {
        this.recipes = [];
      }
      this.recipes.push(recipe);
    };


    this.load = function(other) {
      this.name = other.name;
      this.attrs = other.attributes;
      this.branch = other.branch;
      this.version = other.version;
      this.cookbookHomeUrl = other.cookbookHomeUrl;
      this.github = other.github;
      _addRecipes(this, other.recipes);
    }

  }

  function _Ec2() {
    this.type = null;
    this.image = null;
    this.region = null;
    this.price = null;
    this.vpc = null;
    this.subnet = null;
    this.load = function(other) {
      this.type = other.type || null;
      this.image = other.image || null;
      this.region = other.region || null;
      this.price = other.price || null;
      this.vpc = other.vpc || null;
      this.subnet = other.subnet || null;
    }
  }
  
  function _Baremetal() {
    this.username = null;
    this.ips = null;
    this.load = function(other) {
      this.username = other.username || null;
      this.ips = other.ips || null;
    }
  }

  function _Recipe() {
    this.name = null;
    this.load = function(other) {
      this.name = other.title;
    }
  }

  var coreCluster = new _Cluster();
  coreCluster.load(uiCluster);
  return coreCluster;
}

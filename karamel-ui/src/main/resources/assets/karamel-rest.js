
// ** ====== REST BUILDER METHOD . =========== **//

function getRestObjBuilder () {
    
    var helperObj = _getRestHelperMethods();

    function _getRestHelperMethods() {

        return {
            
            addEc2Provider: function(container, provider){
                var ec2 = new _Ec2Provider();
                ec2.load(provider);
                container.addEc2Provider(ec2);
            },
            
            addCookbooks: function (container, cookbooks) {
                for (var i = 0; i < cookbooks.length; i++) {
                    var cookbook = new _Cookbook();
                    cookbook.load(cookbooks[i]);
                    container.addCookbook(cookbook);
                }
            },

            addGroups: function (container, groups) {
                for (var i = 0; i < groups.length; i++) {
                    var group = new _Group();
                    group.load(groups[i]);
                    container.addGroup(group);
                }
            },

            addRecipes: function (container, recipes) {
                for (var i = 0; i < recipes.length; i++) {
                    var recipe = new _Recipe();
                    recipe.load(recipes[i]);
                    container.addRecipe(recipe);
                }
            }
        }
    }

    function _Cluster() {

        this.name = null;
        this.cookbooks = null;
        this.groups = null;

        // Different Providers, having hard coded attributes
        this.ec2 = null;

        this.addCookbook = function (cookbook) {
            if (this.cookbooks === null) {
                this.cookbooks = [];
            }
            this.cookbooks.push(cookbook);
        };

        this.addGroup = function (group) {
            if (this.groups == null) {
                this.groups = [];
            }
            this.groups.push(group);
        };
        
        this.addEc2Provider = function(provider){
            this.ec2 = provider;
        };
        
        this.load = function (other) {
            this.name = other.name;
            helperObj.addEc2Provider(this, other.ec2Provider);
            helperObj.addCookbooks(this, other.cookbooks);
            helperObj.addGroups(this, other.nodeGroups);
        }

    }


    function _Group() {
        this.name = null;
        this.cookbooks = null;
        this.size = null;
        this.provider = null;

        this.addCookbook = function (cookbook) {
            if (this.cookbooks == null) {
                this.cookbooks = [];
            }
            this.cookbooks.push(cookbook);
        };


        this.load = function (other) {

            this.name = other.name;
            this.size = other.instances;

            helperObj.addCookbooks(this, other.cookbooks);
        }
    }

    function _Cookbook() {

        this.name = null;
        this.attrs = null;
        this.branch = null;
        this.version = null;
        this.cookbookHomeUrl = null;
        this.github = null;

        this.addRecipe = function (recipe) {
            if (this.recipes == null) {
                this.recipes = [];
            }
            this.recipes.push(recipe);
        };


        this.load = function (other) {
            this.name = other.name;
            this.attrs = other.attributes;
            this.branch = other.branch;
            this.version = other.version;
            this.cookbookHomeUrl = other.cookbookHomeUrl;
            this.github = other.github;

            helperObj.addRecipes(this , other.recipes);
        }

    }
    
    function _Ec2Provider(){
        
        this.type = null;
        this.image = null;
        this.region = null;
        this.price = null;
        this.vpc = null;
        this.subnet = null;
        
        this.load = function(other){
            this.type = other.type || null;
            this.image = other.image || null;
            this.region = other.region || null;
            this.price = other.price || null;
            this.vpc = other.vpc || null;
            this.subnet = other.subnet || null;
        }
        
    }

    function _Recipe() {

        this.name = null;
        this.load = function (other) {
            this.name = other.title;
        }
    }

    return {

        buildKaramelForRest : function (board){

            var _rest = new _Cluster();

            if(_.isNull(board) || _.isUndefined(board)){
                return null;
            }

            _rest.load(board);
            return _rest;
        }

    }
}

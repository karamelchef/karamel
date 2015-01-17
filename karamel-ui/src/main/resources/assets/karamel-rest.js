
// ** ====== REST BUILDER METHOD . =========== **//

function getRestObjBuilder () {

    function _getRestHelperMethods() {

        return {
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
        this.provider = null;
        var helperObj = _getRestHelperMethods();


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


        this.load = function (other) {
            this.name = other.name;

            helperObj.addCookbooks(this, other.cookbooks);
            helperObj.addGroups(this, other.nodeGroups);
        }

    }


    function _Group() {
        this.name = null;
        this.cookbooks = null;
        this.size = null;
        this.provider = null;
        var helperObj = _getRestHelperMethods();

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

        var helperObj = _getRestHelperMethods();

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

    function _Recipe() {

        this.name = null;
        this.load = function (other) {
            this.name = other.title;
        }
    }

    return {

        buildCaramelForRest : function (board){

            var _rest = new _Cluster();

            if(_.isNull(board) || _.isUndefined(board)){
                return null;
            }

            _rest.load(board);
            return _rest;
        }

    }
}

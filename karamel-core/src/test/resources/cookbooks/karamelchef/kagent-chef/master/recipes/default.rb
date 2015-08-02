


<!DOCTYPE html>
<html lang="en" class="">
  <head prefix="og: http://ogp.me/ns# fb: http://ogp.me/ns/fb# object: http://ogp.me/ns/object# article: http://ogp.me/ns/article# profile: http://ogp.me/ns/profile#">
    <meta charset='utf-8'>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="Content-Language" content="en">
    <meta name="viewport" content="width=1020">
    
    
    <title>kagent-chef/recipes/default.rb at master · karamelchef/kagent-chef · GitHub</title>
    <link rel="search" type="application/opensearchdescription+xml" href="/opensearch.xml" title="GitHub">
    <link rel="fluid-icon" href="https://github.com/fluidicon.png" title="GitHub">
    <link rel="apple-touch-icon" sizes="57x57" href="/apple-touch-icon-114.png">
    <link rel="apple-touch-icon" sizes="114x114" href="/apple-touch-icon-114.png">
    <link rel="apple-touch-icon" sizes="72x72" href="/apple-touch-icon-144.png">
    <link rel="apple-touch-icon" sizes="144x144" href="/apple-touch-icon-144.png">
    <meta property="fb:app_id" content="1401488693436528">

      <meta content="@github" name="twitter:site" /><meta content="summary" name="twitter:card" /><meta content="karamelchef/kagent-chef" name="twitter:title" /><meta content="Contribute to kagent-chef development by creating an account on GitHub." name="twitter:description" /><meta content="https://avatars0.githubusercontent.com/u/10535788?v=3&amp;s=400" name="twitter:image:src" />
      <meta content="GitHub" property="og:site_name" /><meta content="object" property="og:type" /><meta content="https://avatars0.githubusercontent.com/u/10535788?v=3&amp;s=400" property="og:image" /><meta content="karamelchef/kagent-chef" property="og:title" /><meta content="https://github.com/karamelchef/kagent-chef" property="og:url" /><meta content="Contribute to kagent-chef development by creating an account on GitHub." property="og:description" />
      <meta name="browser-stats-url" content="https://api.github.com/_private/browser/stats">
    <meta name="browser-errors-url" content="https://api.github.com/_private/browser/errors">
    <link rel="assets" href="https://assets-cdn.github.com/">
    
    <meta name="pjax-timeout" content="1000">
    

    <meta name="msapplication-TileImage" content="/windows-tile.png">
    <meta name="msapplication-TileColor" content="#ffffff">
    <meta name="selected-link" value="repo_source" data-pjax-transient>

        <meta name="google-analytics" content="UA-3769691-2">

    <meta content="collector.githubapp.com" name="octolytics-host" /><meta content="collector-cdn.github.com" name="octolytics-script-host" /><meta content="github" name="octolytics-app-id" /><meta content="4F882E1E:44D5:7DD8206:55BE777F" name="octolytics-dimension-request_id" />
    
    <meta content="Rails, view, blob#blame" data-pjax-transient="true" name="analytics-event" />
    <meta class="js-ga-set" name="dimension1" content="Logged Out">
      <meta class="js-ga-set" name="dimension4" content="Current repo nav">
    <meta name="is-dotcom" content="true">
        <meta name="hostname" content="github.com">
    <meta name="user-login" content="">

      <link rel="icon" sizes="any" mask href="https://assets-cdn.github.com/pinned-octocat.svg">
      <meta name="theme-color" content="#4078c0">
      <link rel="icon" type="image/x-icon" href="https://assets-cdn.github.com/favicon.ico">

    <!-- </textarea> --><!-- '"` --><meta content="authenticity_token" name="csrf-param" />
<meta content="/6bto+CR1DGtyah54xkNN+Pfwr3KCpVAkDwW92k9M3R+tvpqGkPUM8GKL/cKC1UsZj3gtcvfGqd2amKccmnM3g==" name="csrf-token" />
    

    <link crossorigin="anonymous" href="https://assets-cdn.github.com/assets/github/index-c7126cd67871e693a9f863b7a0e99879ca39079b15a8784f8b543c03bf14ad72.css" media="all" rel="stylesheet" />
    <link crossorigin="anonymous" href="https://assets-cdn.github.com/assets/github2/index-87247f16e6450ef54cb0eda3f8f1484e33a3f18c7a7d3df1f76f67cba36a8d6d.css" media="all" rel="stylesheet" />
    
    


    <meta http-equiv="x-pjax-version" content="f8fdf7d6713452aadb5c847c2e94f51b">

      
  <meta name="description" content="Contribute to kagent-chef development by creating an account on GitHub.">
  <meta name="go-import" content="github.com/karamelchef/kagent-chef git https://github.com/karamelchef/kagent-chef.git">

  <meta content="10535788" name="octolytics-dimension-user_id" /><meta content="karamelchef" name="octolytics-dimension-user_login" /><meta content="29315797" name="octolytics-dimension-repository_id" /><meta content="karamelchef/kagent-chef" name="octolytics-dimension-repository_nwo" /><meta content="true" name="octolytics-dimension-repository_public" /><meta content="false" name="octolytics-dimension-repository_is_fork" /><meta content="29315797" name="octolytics-dimension-repository_network_root_id" /><meta content="karamelchef/kagent-chef" name="octolytics-dimension-repository_network_root_nwo" />
  <link href="https://github.com/karamelchef/kagent-chef/commits/master.atom" rel="alternate" title="Recent Commits to kagent-chef:master" type="application/atom+xml">

  </head>


  <body class="logged_out  env-production  vis-public">
    <a href="#start-of-content" tabindex="1" class="accessibility-aid js-skip-to-content">Skip to content</a>
    <div class="wrapper">
      
      
      



        
        <div class="header header-logged-out" role="banner">
  <div class="container clearfix">

    <a class="header-logo-wordmark" href="https://github.com/" data-ga-click="(Logged out) Header, go to homepage, icon:logo-wordmark">
      <span class="mega-octicon octicon-logo-github"></span>
    </a>

    <div class="header-actions" role="navigation">
        <a class="btn btn-primary" href="/join" data-ga-click="(Logged out) Header, clicked Sign up, text:sign-up">Sign up</a>
      <a class="btn" href="/login?return_to=%2Fkaramelchef%2Fkagent-chef%2Fblame%2Fmaster%2Frecipes%2Fdefault.rb" data-ga-click="(Logged out) Header, clicked Sign in, text:sign-in">Sign in</a>
    </div>

    <div class="site-search repo-scope js-site-search" role="search">
      <!-- </textarea> --><!-- '"` --><form accept-charset="UTF-8" action="/karamelchef/kagent-chef/search" class="js-site-search-form" data-global-search-url="/search" data-repo-search-url="/karamelchef/kagent-chef/search" method="get"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /></div>
  <label class="js-chromeless-input-container form-control">
    <div class="scope-badge">This repository</div>
    <input type="text"
      class="js-site-search-focus js-site-search-field is-clearable chromeless-input"
      data-hotkey="s"
      name="q"
      placeholder="Search"
      aria-label="Search this repository"
      data-global-scope-placeholder="Search GitHub"
      data-repo-scope-placeholder="Search"
      tabindex="1"
      autocapitalize="off">
  </label>
</form>
    </div>

      <ul class="header-nav left" role="navigation">
          <li class="header-nav-item">
            <a class="header-nav-link" href="/explore" data-ga-click="(Logged out) Header, go to explore, text:explore">Explore</a>
          </li>
          <li class="header-nav-item">
            <a class="header-nav-link" href="/features" data-ga-click="(Logged out) Header, go to features, text:features">Features</a>
          </li>
          <li class="header-nav-item">
            <a class="header-nav-link" href="https://enterprise.github.com/" data-ga-click="(Logged out) Header, go to enterprise, text:enterprise">Enterprise</a>
          </li>
          <li class="header-nav-item">
            <a class="header-nav-link" href="/blog" data-ga-click="(Logged out) Header, go to blog, text:blog">Blog</a>
          </li>
      </ul>

  </div>
</div>



      <div id="start-of-content" class="accessibility-aid"></div>
          <div class="site" itemscope itemtype="http://schema.org/WebPage">
    <div id="js-flash-container">
      
    </div>
    <div class="pagehead repohead instapaper_ignore readability-menu ">
      <div class="container">

        <div class="clearfix">
          
<ul class="pagehead-actions">

  <li>
      <a href="/login?return_to=%2Fkaramelchef%2Fkagent-chef"
    class="btn btn-sm btn-with-count tooltipped tooltipped-n"
    aria-label="You must be signed in to watch a repository" rel="nofollow">
    <span class="octicon octicon-eye"></span>
    Watch
  </a>
  <a class="social-count" href="/karamelchef/kagent-chef/watchers">
    8
  </a>

  </li>

  <li>
      <a href="/login?return_to=%2Fkaramelchef%2Fkagent-chef"
    class="btn btn-sm btn-with-count tooltipped tooltipped-n"
    aria-label="You must be signed in to star a repository" rel="nofollow">
    <span class="octicon octicon-star"></span>
    Star
  </a>

    <a class="social-count js-social-count" href="/karamelchef/kagent-chef/stargazers">
      0
    </a>

  </li>

    <li>
      <a href="/login?return_to=%2Fkaramelchef%2Fkagent-chef"
        class="btn btn-sm btn-with-count tooltipped tooltipped-n"
        aria-label="You must be signed in to fork a repository" rel="nofollow">
        <span class="octicon octicon-repo-forked"></span>
        Fork
      </a>
      <a href="/karamelchef/kagent-chef/network" class="social-count">
        0
      </a>
    </li>
</ul>

          <h1 itemscope itemtype="http://data-vocabulary.org/Breadcrumb" class="entry-title public ">
            <span class="mega-octicon octicon-repo"></span>
            <span class="author"><a href="/karamelchef" class="url fn" itemprop="url" rel="author"><span itemprop="title">karamelchef</span></a></span><!--
         --><span class="path-divider">/</span><!--
         --><strong><a href="/karamelchef/kagent-chef" data-pjax="#js-repo-pjax-container">kagent-chef</a></strong>

            <span class="page-context-loader">
              <img alt="" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
            </span>

          </h1>
        </div>

      </div>
    </div>

      <div class="container">
        <div class="repository-with-sidebar repo-container new-discussion-timeline ">
          <div class="repository-sidebar clearfix">
              

<nav class="sunken-menu repo-nav js-repo-nav js-sidenav-container-pjax js-octicon-loaders"
     role="navigation"
     data-pjax="#js-repo-pjax-container"
     data-issue-count-url="/karamelchef/kagent-chef/issues/counts">
  <ul class="sunken-menu-group">
    <li class="tooltipped tooltipped-w" aria-label="Code">
      <a href="/karamelchef/kagent-chef" aria-label="Code" aria-selected="true" class="js-selected-navigation-item selected sunken-menu-item" data-hotkey="g c" data-selected-links="repo_source repo_downloads repo_commits repo_releases repo_tags repo_branches /karamelchef/kagent-chef">
        <span class="octicon octicon-code"></span> <span class="full-word">Code</span>
        <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>

      <li class="tooltipped tooltipped-w" aria-label="Issues">
        <a href="/karamelchef/kagent-chef/issues" aria-label="Issues" class="js-selected-navigation-item sunken-menu-item" data-hotkey="g i" data-selected-links="repo_issues repo_labels repo_milestones /karamelchef/kagent-chef/issues">
          <span class="octicon octicon-issue-opened"></span> <span class="full-word">Issues</span>
          <span class="js-issue-replace-counter"></span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>      </li>

    <li class="tooltipped tooltipped-w" aria-label="Pull requests">
      <a href="/karamelchef/kagent-chef/pulls" aria-label="Pull requests" class="js-selected-navigation-item sunken-menu-item" data-hotkey="g p" data-selected-links="repo_pulls /karamelchef/kagent-chef/pulls">
          <span class="octicon octicon-git-pull-request"></span> <span class="full-word">Pull requests</span>
          <span class="js-pull-replace-counter"></span>
          <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>

  </ul>
  <div class="sunken-menu-separator"></div>
  <ul class="sunken-menu-group">

    <li class="tooltipped tooltipped-w" aria-label="Pulse">
      <a href="/karamelchef/kagent-chef/pulse" aria-label="Pulse" class="js-selected-navigation-item sunken-menu-item" data-selected-links="pulse /karamelchef/kagent-chef/pulse">
        <span class="octicon octicon-pulse"></span> <span class="full-word">Pulse</span>
        <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>

    <li class="tooltipped tooltipped-w" aria-label="Graphs">
      <a href="/karamelchef/kagent-chef/graphs" aria-label="Graphs" class="js-selected-navigation-item sunken-menu-item" data-selected-links="repo_graphs repo_contributors /karamelchef/kagent-chef/graphs">
        <span class="octicon octicon-graph"></span> <span class="full-word">Graphs</span>
        <img alt="" class="mini-loader" height="16" src="https://assets-cdn.github.com/images/spinners/octocat-spinner-32.gif" width="16" />
</a>    </li>
  </ul>


</nav>

                <div class="only-with-full-nav">
                    
<div class="js-clone-url clone-url open"
  data-protocol-type="http">
  <h3><span class="text-emphasized">HTTPS</span> clone URL</h3>
  <div class="input-group js-zeroclipboard-container">
    <input type="text" class="input-mini input-monospace js-url-field js-zeroclipboard-target"
           value="https://github.com/karamelchef/kagent-chef.git" readonly="readonly" aria-label="HTTPS clone URL">
    <span class="input-group-button">
      <button aria-label="Copy to clipboard" class="js-zeroclipboard btn btn-sm zeroclipboard-button tooltipped tooltipped-s" data-copied-hint="Copied!" type="button"><span class="octicon octicon-clippy"></span></button>
    </span>
  </div>
</div>

  
<div class="js-clone-url clone-url "
  data-protocol-type="subversion">
  <h3><span class="text-emphasized">Subversion</span> checkout URL</h3>
  <div class="input-group js-zeroclipboard-container">
    <input type="text" class="input-mini input-monospace js-url-field js-zeroclipboard-target"
           value="https://github.com/karamelchef/kagent-chef" readonly="readonly" aria-label="Subversion checkout URL">
    <span class="input-group-button">
      <button aria-label="Copy to clipboard" class="js-zeroclipboard btn btn-sm zeroclipboard-button tooltipped tooltipped-s" data-copied-hint="Copied!" type="button"><span class="octicon octicon-clippy"></span></button>
    </span>
  </div>
</div>



  <div class="clone-options">You can clone with
    <!-- </textarea> --><!-- '"` --><form accept-charset="UTF-8" action="/users/set_protocol?protocol_selector=http&amp;protocol_type=clone" class="inline-form js-clone-selector-form " data-form-nonce="32945b71220229f29f6ab6ea5bce69a5790de6f8" data-remote="true" method="post"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /><input name="authenticity_token" type="hidden" value="nAw/gCgsI/k2iwW5xljc3fRMEPJ8ax6fY+QvEn3zfoHXIh4+drivRz3A+7umKBaQTFv7drGIfFubtFleCMaOXw==" /></div><button class="btn-link js-clone-selector" data-protocol="http" type="submit">HTTPS</button></form> or <!-- </textarea> --><!-- '"` --><form accept-charset="UTF-8" action="/users/set_protocol?protocol_selector=subversion&amp;protocol_type=clone" class="inline-form js-clone-selector-form " data-form-nonce="32945b71220229f29f6ab6ea5bce69a5790de6f8" data-remote="true" method="post"><div style="margin:0;padding:0;display:inline"><input name="utf8" type="hidden" value="&#x2713;" /><input name="authenticity_token" type="hidden" value="vGQnIFIuPor/fWHAAhKQZLWry2rAwwCGbtzSDhfgstsyIGgsJSsNIAd11FVxs6mQ+HWNA91LZbisxp639BwLYg==" /></div><button class="btn-link js-clone-selector" data-protocol="subversion" type="submit">Subversion</button></form>.
    <a href="https://help.github.com/articles/which-remote-url-should-i-use" class="help tooltipped tooltipped-n" aria-label="Get help on which URL is right for you.">
      <span class="octicon octicon-question"></span>
    </a>
  </div>

                  <a href="/karamelchef/kagent-chef/archive/master.zip"
                     class="btn btn-sm sidebar-button"
                     aria-label="Download the contents of karamelchef/kagent-chef as a zip file"
                     title="Download the contents of karamelchef/kagent-chef as a zip file"
                     rel="nofollow">
                    <span class="octicon octicon-cloud-download"></span>
                    Download ZIP
                  </a>
                </div>
          </div>
          <div id="js-repo-pjax-container" class="repository-content context-loader-container" data-pjax-container>

            
<a href="/karamelchef/kagent-chef/blame/4cec85d82e215c1f83b025bf9f9d844704c1d236/recipes/default.rb" class="hidden js-permalink-shortcut" data-hotkey="y">Permalink</a>

<div class="breadcrumb css-truncate blame-breadcrumb js-zeroclipboard-container">
  <span class="css-truncate-target js-zeroclipboard-target"><span class="repo-root js-repo-root"><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/karamelchef/kagent-chef" class="" data-branch="master" data-pjax="true" itemscope="url"><span itemprop="title">kagent-chef</span></a></span></span><span class="separator">/</span><span itemscope="" itemtype="http://data-vocabulary.org/Breadcrumb"><a href="/karamelchef/kagent-chef/tree/master/recipes" class="" data-branch="master" data-pjax="true" itemscope="url"><span itemprop="title">recipes</span></a></span><span class="separator">/</span><strong class="final-path">default.rb</strong></span>
  <button aria-label="Copy file path to clipboard" class="js-zeroclipboard btn btn-sm zeroclipboard-button tooltipped tooltipped-s" data-copied-hint="Copied!" type="button"><span class="octicon octicon-clippy"></span></button>
</div>

<div class="line-age-legend">
  <span>Newer</span>
  <ol>
      <li class="heat" data-heat="1"></li>
      <li class="heat" data-heat="2"></li>
      <li class="heat" data-heat="3"></li>
      <li class="heat" data-heat="4"></li>
      <li class="heat" data-heat="5"></li>
      <li class="heat" data-heat="6"></li>
      <li class="heat" data-heat="7"></li>
      <li class="heat" data-heat="8"></li>
      <li class="heat" data-heat="9"></li>
      <li class="heat" data-heat="10"></li>
  </ol>
  <span>Older</span>
</div>

<div class="file">
  <div class="file-header">
    <div class="file-actions">
      <div class="btn-group">
        <a href="/karamelchef/kagent-chef/raw/master/recipes/default.rb" class="btn btn-sm" id="raw-url">Raw</a>
        <a href="/karamelchef/kagent-chef/blob/master/recipes/default.rb" class="btn btn-sm js-update-url-with-hash">Normal view</a>
        <a href="/karamelchef/kagent-chef/commits/master/recipes/default.rb" class="btn btn-sm" rel="nofollow">History</a>
      </div>
    </div>



    <div class="file-info">
      <span class="octicon octicon-file-text"></span>
      <span class="file-mode" title="File Mode">100644</span>
      <span class="file-info-divider"></span>
        123 lines (105 sloc)
        <span class="file-info-divider"></span>
      3.041 kB
    </div>
  </div>

  <div class="blob-wrapper">
    <table class="blame-container highlight data js-file-line-container">
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="22">
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-sha">dc9398e</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-commit-title" title="initial import from hopstart repo of hopagent">initial import from hopstart repo of hopagent</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2014-11-01T22:33:00Z" is="relative-time">Nov 1, 2014</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L1">1</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC1"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L2">2</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC2">service <span class="pl-s"><span class="pl-pds">&quot;</span>kagent<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L3">3</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC3">  supports <span class="pl-c1">:restart</span> =&gt; <span class="pl-c1">true</span>, <span class="pl-c1">:start</span> =&gt; <span class="pl-c1">true</span>, <span class="pl-c1">:stop</span> =&gt; <span class="pl-c1">true</span>, <span class="pl-c1">:enable</span> =&gt; <span class="pl-c1">true</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L4">4</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC4"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L5">5</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC5"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L6">6</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC6">template <span class="pl-s"><span class="pl-pds">&quot;</span>/etc/init.d/kagent<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L7">7</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC7">  source <span class="pl-s"><span class="pl-pds">&quot;</span>kagent.erb<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L8">8</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC8">  owner node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L9">9</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC9">  group node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L10">10</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC10">  mode <span class="pl-c1">0655</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L11">11</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC11">  notifies <span class="pl-c1">:enable</span>, <span class="pl-s"><span class="pl-pds">&quot;</span>service[kagent]<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L12">12</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC12"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L13">13</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC13"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L14">14</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC14">template<span class="pl-s"><span class="pl-pds">&quot;</span><span class="pl-pse">#{</span><span class="pl-s1">node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:base_dir</span>]</span><span class="pl-pse"><span class="pl-s1">}</span></span>/agent.py<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L15">15</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC15">  source <span class="pl-s"><span class="pl-pds">&quot;</span>agent.py.erb<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L16">16</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC16">  owner node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L17">17</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC17">  group node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L18">18</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC18">  mode <span class="pl-c1">0655</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L19">19</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC19">  notifies <span class="pl-c1">:enable</span>, <span class="pl-s"><span class="pl-pds">&quot;</span>service[kagent]<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L20">20</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC20"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L21">21</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC21"></td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="2">
            <a href="/karamelchef/kagent-chef/commit/78fd84eac2f18f2a6066fcaf8dc73a5c2eb785cb" class="blame-sha">78fd84e</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/78fd84eac2f18f2a6066fcaf8dc73a5c2eb785cb" class="blame-commit-title" title="renamed to kagent, moved collectd scripts to collectd cookbook">renamed to kagent, moved collectd scripts to collectd cookbook</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2014-11-02T17:42:09Z" is="relative-time">Nov 2, 2014</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L22">22</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC22">[<span class="pl-s"><span class="pl-pds">&#39;</span>start-agent.sh<span class="pl-pds">&#39;</span></span>, <span class="pl-s"><span class="pl-pds">&#39;</span>stop-agent.sh<span class="pl-pds">&#39;</span></span>, <span class="pl-s"><span class="pl-pds">&#39;</span>restart-agent.sh<span class="pl-pds">&#39;</span></span>, <span class="pl-s"><span class="pl-pds">&#39;</span>get-pid.sh<span class="pl-pds">&#39;</span></span>].each <span class="pl-k">do </span>|<span class="pl-smi">script</span>|</td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="25">
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-sha">dc9398e</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-commit-title" title="initial import from hopstart repo of hopagent">initial import from hopstart repo of hopagent</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2014-11-01T22:33:00Z" is="relative-time">Nov 1, 2014</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L23">23</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC23">  <span class="pl-c1">Chef</span>::<span class="pl-c1">Log</span>.info <span class="pl-s"><span class="pl-pds">&quot;</span>Installing <span class="pl-pse">#{</span><span class="pl-s1">script</span><span class="pl-pse"><span class="pl-s1">}</span></span><span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L24">24</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC24">  template <span class="pl-s"><span class="pl-pds">&quot;</span><span class="pl-pse">#{</span><span class="pl-s1">node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:base_dir</span>]</span><span class="pl-pse"><span class="pl-s1">}</span></span>/<span class="pl-pse">#{</span><span class="pl-s1">script</span><span class="pl-pse"><span class="pl-s1">}</span></span><span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L25">25</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC25">    source <span class="pl-s"><span class="pl-pds">&quot;</span><span class="pl-pse">#{</span><span class="pl-s1">script</span><span class="pl-pse"><span class="pl-s1">}</span></span>.erb<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L26">26</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC26">    owner node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L27">27</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC27">    group node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L28">28</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC28">    mode <span class="pl-c1">0655</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L29">29</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC29">  <span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L30">30</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC30"><span class="pl-k">end</span> </td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L31">31</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC31"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L32">32</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC32">[<span class="pl-s"><span class="pl-pds">&#39;</span>services<span class="pl-pds">&#39;</span></span>].each <span class="pl-k">do </span>|<span class="pl-smi">conf</span>|</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L33">33</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC33">  <span class="pl-c1">Chef</span>::<span class="pl-c1">Log</span>.info <span class="pl-s"><span class="pl-pds">&quot;</span>Installing <span class="pl-pse">#{</span><span class="pl-s1">conf</span><span class="pl-pse"><span class="pl-s1">}</span></span><span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L34">34</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC34">  template <span class="pl-s"><span class="pl-pds">&quot;</span><span class="pl-pse">#{</span><span class="pl-s1">node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:base_dir</span>]</span><span class="pl-pse"><span class="pl-s1">}</span></span>/<span class="pl-pse">#{</span><span class="pl-s1">conf</span><span class="pl-pse"><span class="pl-s1">}</span></span><span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L35">35</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC35">    source <span class="pl-s"><span class="pl-pds">&quot;</span><span class="pl-pse">#{</span><span class="pl-s1">conf</span><span class="pl-pse"><span class="pl-s1">}</span></span>.erb<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L36">36</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC36">    owner node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L37">37</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC37">    group node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L38">38</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC38">    mode <span class="pl-c1">0644</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L39">39</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC39">  <span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L40">40</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC40"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L41">41</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC41"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L42">42</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC42">private_ip <span class="pl-k">=</span> my_private_ip()</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L43">43</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC43">public_ip <span class="pl-k">=</span> my_public_ip()</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L44">44</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC44"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L45">45</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC45">dashboard_endpoint <span class="pl-k">=</span> node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:dashboard</span>][<span class="pl-c1">:ip_port</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L46">46</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC46"><span class="pl-k">if</span> dashboard_endpoint.eql? <span class="pl-s"><span class="pl-pds">&quot;</span><span class="pl-pds">&quot;</span></span></td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="4">
            <a href="/karamelchef/kagent-chef/commit/415b5ac9c032c69e7bbf5409ca017945553c0e08" class="blame-sha">415b5ac</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/415b5ac9c032c69e7bbf5409ca017945553c0e08" class="blame-commit-title" title="kmon no longer required attribute">kmon no longer required attribute</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2015-01-05T23:00:27Z" is="relative-time">Jan 6, 2015</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L47">47</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC47">  <span class="pl-k">if</span> node.attribute? <span class="pl-s"><span class="pl-pds">&quot;</span>kmon<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L48">48</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC48">    dashboard_endpoint <span class="pl-k">=</span> private_cookbook_ip(<span class="pl-s"><span class="pl-pds">&quot;</span>kmon<span class="pl-pds">&quot;</span></span>)  <span class="pl-k">+</span> <span class="pl-s"><span class="pl-pds">&quot;</span>:8080<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L49">49</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC49">  <span class="pl-k">end</span></td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="58">
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-sha">dc9398e</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-commit-title" title="initial import from hopstart repo of hopagent">initial import from hopstart repo of hopagent</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2014-11-01T22:33:00Z" is="relative-time">Nov 1, 2014</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L50">50</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC50"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L51">51</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC51"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L52">52</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC52">template <span class="pl-s"><span class="pl-pds">&quot;</span><span class="pl-pse">#{</span><span class="pl-s1">node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:base_dir</span>]</span><span class="pl-pse"><span class="pl-s1">}</span></span>/config.ini<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L53">53</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC53">  source <span class="pl-s"><span class="pl-pds">&quot;</span>config.ini.erb<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L54">54</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC54">  owner node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L55">55</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC55">  group node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:run_as_user</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L56">56</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC56">  mode <span class="pl-c1">0600</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L57">57</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC57">  variables({</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L58">58</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC58">              <span class="pl-c1">:rest_url</span> =&gt; <span class="pl-s"><span class="pl-pds">&quot;</span>http://<span class="pl-pse">#{</span><span class="pl-s1">dashboard_endpoint</span><span class="pl-pse"><span class="pl-s1">}</span></span>/<span class="pl-pse">#{</span><span class="pl-s1">node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:dashboard_app</span>]</span><span class="pl-pse"><span class="pl-s1">}</span></span><span class="pl-pds">&quot;</span></span>,</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L59">59</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC59">              <span class="pl-c1">:rack</span> =&gt; <span class="pl-s"><span class="pl-pds">&#39;</span>/default<span class="pl-pds">&#39;</span></span>,</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L60">60</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC60">              <span class="pl-c1">:public_ip</span> =&gt; public_ip,</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L61">61</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC61">              <span class="pl-c1">:private_ip</span> =&gt; private_ip</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L62">62</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC62">            })</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L63">63</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC63">  notifies <span class="pl-c1">:restart</span>, <span class="pl-s"><span class="pl-pds">&quot;</span>service[kagent]<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L64">64</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC64"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L65">65</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC65"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L66">66</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC66"><span class="pl-c"># TODO install MONIT to restart the agent if it crashes</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L67">67</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC67"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L68">68</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC68">bash <span class="pl-s"><span class="pl-pds">&quot;</span>start_kagent<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L69">69</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC69">  user <span class="pl-s"><span class="pl-pds">&quot;</span>root<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L70">70</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC70">  code <span class="pl-s"><span class="pl-pds">&lt;&lt;-EOF</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L71">71</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC71"><span class="pl-s">   service kagent restart</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L72">72</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC72"><span class="pl-s"><span class="pl-pds"> EOF</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L73">73</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC73"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L74">74</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC74"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L75">75</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC75"><span class="pl-k">case</span> node[<span class="pl-c1">:platform_family</span>]</td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L76">76</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC76"><span class="pl-k">when</span> <span class="pl-s"><span class="pl-pds">&quot;</span>rhel<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L77">77</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC77"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L78">78</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC78">  bash <span class="pl-s"><span class="pl-pds">&quot;</span>disable-iptables<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L79">79</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC79">    code <span class="pl-s"><span class="pl-pds">&lt;&lt;-EOH</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L80">80</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC80"><span class="pl-s">    service iptables stop</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L81">81</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC81"><span class="pl-s"><span class="pl-pds">  EOH</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L82">82</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC82">    only_if <span class="pl-s"><span class="pl-pds">&quot;</span>test -f /etc/init.d/iptables &amp;&amp; service iptables status<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L83">83</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC83">  <span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L84">84</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC84"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L85">85</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC85"><span class="pl-k">if</span> node[<span class="pl-c1">:instance_role</span>] <span class="pl-k">==</span> <span class="pl-s"><span class="pl-pds">&#39;</span>vagrant<span class="pl-pds">&#39;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L86">86</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC86">  bash <span class="pl-s"><span class="pl-pds">&quot;</span>fix-sudoers-for-vagrant<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L87">87</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC87">    code <span class="pl-s"><span class="pl-pds">&lt;&lt;-EOH</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L88">88</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC88"><span class="pl-s">    echo &quot;&quot; &gt;&gt; /etc/sudoers</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L89">89</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC89"><span class="pl-s">    echo &quot;#includedir /etc/sudoers.d&quot; &gt;&gt; /etc/sudoers</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L90">90</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC90"><span class="pl-s">    echo &quot;&quot; &gt;&gt; /etc/sudoers</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L91">91</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC91"><span class="pl-s">    touch /etc/sudoers.d/.vagrant_fix</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L92">92</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC92"><span class="pl-s"><span class="pl-pds">  EOH</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L93">93</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC93">    only_if <span class="pl-s"><span class="pl-pds">&quot;</span>test -f /etc/sudoers.d/.vagrant_fix<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L94">94</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC94">  <span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L95">95</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC95"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L96">96</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC96"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L97">97</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC97"><span class="pl-c"># Fix sudoers to allow root exec shell commands for Centos</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L98">98</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC98">node.default[<span class="pl-s"><span class="pl-pds">&#39;</span>authorization<span class="pl-pds">&#39;</span></span>][<span class="pl-s"><span class="pl-pds">&#39;</span>sudo<span class="pl-pds">&#39;</span></span>][<span class="pl-s"><span class="pl-pds">&#39;</span>include_sudoers_d<span class="pl-pds">&#39;</span></span>]<span class="pl-k">=</span><span class="pl-c1">true</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L99">99</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC99"><span class="pl-c"># default &#39;commands&#39; attribute for this LWRP is &#39;ALL&#39;</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L100">100</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC100">sudo <span class="pl-s"><span class="pl-pds">&#39;</span>root<span class="pl-pds">&#39;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L101">101</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC101">  user      <span class="pl-s"><span class="pl-pds">&quot;</span>root<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L102">102</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC102">  runas     <span class="pl-s"><span class="pl-pds">&#39;</span>ALL:ALL<span class="pl-pds">&#39;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L103">103</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC103"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L104">104</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC104"></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L105">105</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC105"><span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L106">106</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC106"></td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="3">
            <a href="/karamelchef/kagent-chef/commit/f551aebdb69ad507acebc46061a49bd78582b02b" class="blame-sha">f551aeb</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/f551aebdb69ad507acebc46061a49bd78582b02b" class="blame-commit-title" title="kitchen working">kitchen working</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2014-11-01T23:39:58Z" is="relative-time">Nov 2, 2014</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L107">107</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC107"><span class="pl-k">if</span> node[<span class="pl-c1">:kagent</span>][<span class="pl-c1">:allow_kmon_ssh_access</span>] <span class="pl-k">==</span> <span class="pl-s"><span class="pl-pds">&#39;</span>true<span class="pl-pds">&#39;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L108">108</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC108"></td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="13">
            <a href="/karamelchef/kagent-chef/commit/415b5ac9c032c69e7bbf5409ca017945553c0e08" class="blame-sha">415b5ac</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/415b5ac9c032c69e7bbf5409ca017945553c0e08" class="blame-commit-title" title="kmon no longer required attribute">kmon no longer required attribute</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2015-01-05T23:00:27Z" is="relative-time">Jan 5, 2015</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L109">109</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC109">  <span class="pl-k">if</span> node.attribute? <span class="pl-s"><span class="pl-pds">&quot;</span>kmon<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L110">110</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC110">    <span class="pl-k">if</span> node[<span class="pl-c1">:kmon</span>].attribute? <span class="pl-s"><span class="pl-pds">&quot;</span>public_key<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L111">111</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC111">      bash <span class="pl-s"><span class="pl-pds">&quot;</span>add_dashboards_public_key<span class="pl-pds">&quot;</span></span> <span class="pl-k">do</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L112">112</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC112">        user <span class="pl-s"><span class="pl-pds">&quot;</span>root<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L113">113</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC113">        code <span class="pl-s"><span class="pl-pds">&lt;&lt;-EOF</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L114">114</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC114"><span class="pl-s">         mkdir -p /root/.ssh</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L115">115</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC115"><span class="pl-s">         chmod 700 /root/.ssh</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L116">116</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC116"><span class="pl-s">         cat <span class="pl-pse">#{</span><span class="pl-s1">node[<span class="pl-c1">:kmon</span>][<span class="pl-c1">:public_key</span>]</span><span class="pl-pse"><span class="pl-s1">}</span></span> &gt;&gt; /root/.ssh/authorized_keys</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L117">117</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC117"><span class="pl-s"><span class="pl-pds">        EOF</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L118">118</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC118">        not_if <span class="pl-s"><span class="pl-pds">&quot;</span>test -f /root/.ssh/authorized_keys<span class="pl-pds">&quot;</span></span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L119">119</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC119">      <span class="pl-k">end</span></td>
          </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L120">120</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC120">    <span class="pl-k">end</span></td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="2">
            <a href="/karamelchef/kagent-chef/commit/f551aebdb69ad507acebc46061a49bd78582b02b" class="blame-sha">f551aeb</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/f551aebdb69ad507acebc46061a49bd78582b02b" class="blame-commit-title" title="kitchen working">kitchen working</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2014-11-01T23:39:58Z" is="relative-time">Nov 1, 2014</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L121">121</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC121">  <span class="pl-k">end</span></td>
          </tr>
        <tr class="blame-commit">
          <td class="blame-commit-info" rowspan="2">
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-sha">dc9398e</a>
            <img alt="@jimdowling" class="avatar blame-commit-avatar" height="32" src="https://avatars0.githubusercontent.com/u/1904928?v=3&amp;s=64" width="32" />
            <a href="/karamelchef/kagent-chef/commit/dc9398e243619793c0a7d2e98f4aaa061387f6da" class="blame-commit-title" title="initial import from hopstart repo of hopagent">initial import from hopstart repo of hopagent</a>
            <div class="blame-commit-meta">
              <a href="/jimdowling" class="muted-link" rel="contributor">jimdowling</a> authored
              <time datetime="2014-11-01T22:33:00Z" is="relative-time">Nov 1, 2014</time>
            </div>
          </td>
        </tr>
          <tr class="blame-line">
            <td class="line-age heat" data-heat="10"></td>
            <td class="blob-num blame-blob-num js-line-number" id="L122">122</td>
            <td class="blob-code blob-code-inner js-file-line" id="LC122"><span class="pl-k">end</span></td>
          </tr>
    </table>
  </div>
</div>


          </div>
        </div>
        <div class="modal-backdrop"></div>
      </div>
  </div>


    </div><!-- /.wrapper -->

      <div class="container">
  <div class="site-footer" role="contentinfo">
    <ul class="site-footer-links right">
        <li><a href="https://status.github.com/" data-ga-click="Footer, go to status, text:status">Status</a></li>
      <li><a href="https://developer.github.com" data-ga-click="Footer, go to api, text:api">API</a></li>
      <li><a href="https://training.github.com" data-ga-click="Footer, go to training, text:training">Training</a></li>
      <li><a href="https://shop.github.com" data-ga-click="Footer, go to shop, text:shop">Shop</a></li>
        <li><a href="https://github.com/blog" data-ga-click="Footer, go to blog, text:blog">Blog</a></li>
        <li><a href="https://github.com/about" data-ga-click="Footer, go to about, text:about">About</a></li>
        <li><a href="https://help.github.com" data-ga-click="Footer, go to help, text:help">Help</a></li>

    </ul>

    <a href="https://github.com" aria-label="Homepage">
      <span class="mega-octicon octicon-mark-github" title="GitHub"></span>
</a>
    <ul class="site-footer-links">
      <li>&copy; 2015 <span title="0.09396s from github-fe120-cp1-prd.iad.github.net">GitHub</span>, Inc.</li>
        <li><a href="https://github.com/site/terms" data-ga-click="Footer, go to terms, text:terms">Terms</a></li>
        <li><a href="https://github.com/site/privacy" data-ga-click="Footer, go to privacy, text:privacy">Privacy</a></li>
        <li><a href="https://github.com/security" data-ga-click="Footer, go to security, text:security">Security</a></li>
        <li><a href="https://github.com/contact" data-ga-click="Footer, go to contact, text:contact">Contact</a></li>
    </ul>
  </div>
</div>


    <div class="fullscreen-overlay js-fullscreen-overlay" id="fullscreen_overlay">
  <div class="fullscreen-container js-suggester-container">
    <div class="textarea-wrap">
      <textarea name="fullscreen-contents" id="fullscreen-contents" class="fullscreen-contents js-fullscreen-contents" placeholder="" aria-label=""></textarea>
      <div class="suggester-container">
        <div class="suggester fullscreen-suggester js-suggester js-navigation-container"></div>
      </div>
    </div>
  </div>
  <div class="fullscreen-sidebar">
    <a href="#" class="exit-fullscreen js-exit-fullscreen tooltipped tooltipped-w" aria-label="Exit Zen Mode">
      <span class="mega-octicon octicon-screen-normal"></span>
    </a>
    <a href="#" class="theme-switcher js-theme-switcher tooltipped tooltipped-w"
      aria-label="Switch themes">
      <span class="octicon octicon-color-mode"></span>
    </a>
  </div>
</div>



    
    

    <div id="ajax-error-message" class="flash flash-error">
      <span class="octicon octicon-alert"></span>
      <a href="#" class="octicon octicon-x flash-close js-ajax-error-dismiss" aria-label="Dismiss error"></a>
      Something went wrong with that request. Please try again.
    </div>


      <script crossorigin="anonymous" src="https://assets-cdn.github.com/assets/frameworks-eedcd4970c51d77d26b12825fc1fb1fbd554a880c0a8649a9cac6b63f1ee7cff.js"></script>
      <script async="async" crossorigin="anonymous" src="https://assets-cdn.github.com/assets/github/index-1af8eb3fd83c34afcee37eae4704e57d3bb35ccacee5574545665527ae02d731.js"></script>
      
      
  </body>
</html>


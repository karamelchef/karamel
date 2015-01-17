/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('demoApp')
    .service('BoardDataService', ['$log', function ($log) {

        var boardDataObject = {
            "name": "Hops Cluster",
            "numberOfColumns": 4,
            "nodeGroups": [
                {"name": "Nodes: MetaData", "recipes": [
                    {"title": "hadoop::nn (4: red)"},
                    {"title": "hadoop::rm (4: red)"},
                    {"title": "ndb::mgmd (1: red)"},
                    {"title": "ndb::mysqld (3: red)"}
                ]},
                {"name": "Nodes: Dashboard", "recipes": [
                    {"title": "hopsdashboard (4: red)",
                        "details": "Testing Card Details"},
                    {"title": "ndb::mysqld ndb::mysqld (3: red)",
                        "details": "Testing Card Details"},
                    {"title": "hadoop::jhs (5: red)"},
                    {"title": "collectd::server (1: black)",
                        "details": "Testing Card Details"}
                ]},
                {"name": "Nodes: DB", "recipes": [
                    {"title": "ndb::ndbd (2: red)",
                        "details": "Testing Card Details"}
                ]},
                {"name": "Nodes: Data", "recipes": [
                    {"title": "hadoop::dn (1: blue)",
                        "details": "Testing Card Details"},
                    {"title": "hadoop::nm (1: black)",
                        "details": "Testing Card Details"}
                ]}
            ]
        };


        var updatedBoardObject = {

            "name": "ReferenceYaml",
            "groups": [
                {
                    "name": "dashboard",
                    "size": 1,
                    "recipes": [
                        "ndb::mysqld",
                        "hopdashboard",
                        "hopagent"
                    ],
                    "ec2": {
                        "type": "m3.large"
                    },
                    "attrs": {
                        "ndb": {
                            "mysqld": 3306
                        }
                    },
                    "cookbooks": {}
                },
                {
                    "name": "namenodes",
                    "size": 2,
                    "recipes": [
                        "ndb::mysqld",
                        "hop::jhs",
                        "hopagent",
                        "hop::rm",
                        "ndb::memcached",
                        "hop::nn",
                        "ndb::mgmd"
                    ],
                    "ec2": {
                        "type": "m3.medium"
                    },
                    "attrs": {},
                    "cookbooks": {}
                },
                {
                    "name": "ndb",
                    "size": 2,
                    "recipes": [
                        "hopagent",
                        "ndb::ndbd"
                    ],
                    "ec2": {
                        "type": "m3.medium"
                    },
                    "attrs": {},
                    "cookbooks": {}
                },
                {
                    "name": "datanodes",
                    "size": 4,
                    "recipes": [
                        "hopagent",
                        "hop::nm",
                        "hop::dn"
                    ],
                    "ec2": {
                        "type": "m3.medium"
                    },
                    "attrs": {},
                    "cookbooks": {}
                }
            ],
            "ec2": {
                "type": "m3.small",
                "region": "eu-west-1",
                "image": "ami-0307ce74",
                "username": "ubuntu"
            },
            "attrs": {
                "ndb": {
                    "ndbapi": {
                        "private_ips": "$ndb.private_ips",
                        "public_ips": "$ndb.public_ips"
                    },
                    "nn": {
                        "jmxport": 8077,
                        "http_port": 50070
                    },
                    "mgmd": {
                        "port": 1186
                    },
                    "ndbd": {
                        "port": 10000
                    }
                },
                "hop": {
                    "dn": {
                        "http_port": 50075
                    },
                    "yarn": {
                        "ps_port": 20888
                    },
                    "rm": {
                        "http_port": 8088,
                        "jmxport": 8042
                    },
                    "nm": {
                        "jmxport": 8083,
                        "http_port": 8042
                    },
                    "jhs": {
                        "http_port": 19888
                    }
                }
            },
            "cookbooks": {
                "highway": {
                    "github": "biobankcloud/highway-chef",
                    "attributes": {
                        "highway/user": "admin",
                        "hopagent/namenode/addrs": "[10.0.1],[10.0.2]"
                    }
                },
                "hopagent": {
                    "github": "hopstart/hopagent-chef",
                    "branch": "master",
                    "attributes": {
                        "hopagent/version": "3.2.0",
                        "hopagent/user": "hopagent"

                    }
                },
                "hop": {
                    "github": "hopstart/hop-chef",
                    "version": "v0.1",
                    "attributes": {
                        "hop/version": "4.1.0",
                        "yarn/resourcemanager": "[192.16.143.2]"
                    }
                }
            }
        };


        var cookbookMetaData =
            [
                {
                    "name": "hopagent",
                    "description": "Installs and configure Hop Agent.",
                    "version": "1.0",
                    "recipes": [
                        {
                            "name": "hadoop::nn",
                            "description": "Installs a Hadoop Namenode"
                        },
                        {
                            "name": "hadoop::dn",
                            "description": "Installs a Hadoop Namenode"
                        },
                        {
                            "name": "hadoop::rm",
                            "description": "Installs a YARN ResourceManager"
                        },
                        {
                            "name": "hadoop::nm",
                            "description": "Installs a YARN NodeManager"
                        },
                        {
                            "name": "hadoop::jhs",
                            "description": "Installs a MapReduce History Server for YARN"
                        },
                        {
                            "name": "hadoop::ps",
                            "description": "Installs a WebProxy Server for YARN"
                        }
                    ],
                    "attributes": [
                        {
                            "name": "hopagent/version",
                            "displayName": "Hadoop version",
                            "type": "string",
                            "description": "Version of hadoop",
                            "default": "2.2.0",
                            "required": "required"
                        },
                        {
                            "name": "hopagent/namenode/addrs",
                            "displayName": "Namenode ip addresses (comma-separated)",
                            "type": "array",
                            "description": "A comma-separated list of Namenode ip address",
                            "required": "optional"
                        },
                        {
                            "name": "yarn/resourcemanager",
                            "displayName": "Ip address",
                            "type": "string",
                            "description": "Ip address for the resourcemanager",
                            "required": "recommended"
                        },
                        {
                            "name": "hopagent/user",
                            "displayName": "Username to run hadoop as",
                            "type": "string",
                            "description": "Username to run hadoop as"
                        },
                        {
                            "name": "hadoop/format",
                            "displayName": "Format HDFS",
                            "type": "string",
                            "description": "Format HDFS, Run \u0027hdfs namenode -format\u0027",
                            "default": "true"
                        },
                        {
                            "name": "hadoop/nn/public_ips",
                            "displayName": "Public ips for NameNodes",
                            "type": "array",
                            "description": "Public ips of Namenodes",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/nn/private_ips",
                            "displayName": "Public ips for NameNodes",
                            "type": "array",
                            "description": "Public ips of Namenodes",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/rm/public_ips",
                            "displayName": "Public ips for ResourceManagers",
                            "type": "array",
                            "description": "Public ips of ResourceManagers",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/nn/private_ips",
                            "displayName": "Public ips for ResourceManagers",
                            "type": "array",
                            "description": "Public ips of ResourceManagers",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/public_ips",
                            "displayName": "Public ips for these nodes",
                            "type": "array",
                            "description": "Public ips of nodes in this group",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/private_ips",
                            "displayName": "Private ips for these nodes",
                            "type": "array",
                            "description": "Private ips of nodes in this group",
                            "default": "[10.0.2.15]"
                        }
                    ]
                },

                {
                    "name": "hop",
                    "description": "Installs and configure Hop.",
                    "version": "1.0",
                    "recipes": [
                        {
                            "name": "hadoop::nn",
                            "description": "Installs a Hadoop Namenode"
                        },
                        {
                            "name": "hadoop::dn",
                            "description": "Installs a Hadoop Namenode"
                        },
                        {
                            "name": "hadoop::rm",
                            "description": "Installs a YARN ResourceManager"
                        },
                        {
                            "name": "hadoop::nm",
                            "description": "Installs a YARN NodeManager"
                        },
                        {
                            "name": "hadoop::jhs",
                            "description": "Installs a MapReduce History Server for YARN"
                        },
                        {
                            "name": "hadoop::ps",
                            "description": "Installs a WebProxy Server for YARN"
                        }
                    ],
                    "attributes": [
                        {
                            "name": "hop/version",
                            "displayName": "Hadoop version",
                            "type": "string",
                            "description": "Version of hadoop",
                            "default": "2.2.0",
                            "required": "required"
                        },
                        {
                            "name": "hop/namenode/addrs",
                            "displayName": "Namenode ip addresses (comma-separated)",
                            "type": "array",
                            "description": "A comma-separated list of Namenode ip address",
                            "required": "optional"
                        },
                        {
                            "name": "yarn/resourcemanager",
                            "displayName": "Ip address",
                            "type": "string",
                            "description": "Ip address for the resourcemanager",
                            "required": "recommended"
                        },
                        {
                            "name": "hadoop/user",
                            "displayName": "Username to run hadoop as",
                            "type": "string",
                            "description": "Username to run hadoop as"
                        },
                        {
                            "name": "hadoop/format",
                            "displayName": "Format HDFS",
                            "type": "string",
                            "description": "Format HDFS, Run \u0027hdfs namenode -format\u0027",
                            "default": "true"
                        },
                        {
                            "name": "hadoop/nn/public_ips",
                            "displayName": "Public ips for NameNodes",
                            "type": "array",
                            "description": "Public ips of Namenodes",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/nn/private_ips",
                            "displayName": "Public ips for NameNodes",
                            "type": "array",
                            "description": "Public ips of Namenodes",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/rm/public_ips",
                            "displayName": "Public ips for ResourceManagers",
                            "type": "array",
                            "description": "Public ips of ResourceManagers",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/nn/private_ips",
                            "displayName": "Public ips for ResourceManagers",
                            "type": "array",
                            "description": "Public ips of ResourceManagers",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/public_ips",
                            "displayName": "Public ips for these nodes",
                            "type": "array",
                            "description": "Public ips of nodes in this group",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/private_ips",
                            "displayName": "Private ips for these nodes",
                            "type": "array",
                            "description": "Private ips of nodes in this group",
                            "default": "[10.0.2.15]"
                        }
                    ]
                },

                {
                    "name": "highway",
                    "description": "Installs and configure Highway.",
                    "version": "1.0",
                    "recipes": [
                        {
                            "name": "hadoop::nn",
                            "description": "Installs a Hadoop Namenode"
                        },
                        {
                            "name": "hadoop::dn",
                            "description": "Installs a Hadoop Namenode"
                        },
                        {
                            "name": "hadoop::rm",
                            "description": "Installs a YARN ResourceManager"
                        },
                        {
                            "name": "hadoop::nm",
                            "description": "Installs a YARN NodeManager"
                        },
                        {
                            "name": "hadoop::jhs",
                            "description": "Installs a MapReduce History Server for YARN"
                        },
                        {
                            "name": "hadoop::ps",
                            "description": "Installs a WebProxy Server for YARN"
                        }
                    ],
                    "attributes": [
                        {
                            "name": "highway/version",
                            "displayName": "Hadoop version",
                            "type": "string",
                            "description": "Version of hadoop",
                            "default": "2.2.0",
                            "required": "required"
                        },
                        {
                            "name": "highway/namenode/addrs",
                            "displayName": "Namenode ip addresses (comma-separated)",
                            "type": "array",
                            "description": "A comma-separated list of Namenode ip address",
                            "required": "optional"
                        },
                        {
                            "name": "yarn/resourcemanager",
                            "displayName": "Ip address",
                            "type": "string",
                            "description": "Ip address for the resourcemanager",
                            "required": "recommended"
                        },
                        {
                            "name": "highway/user",
                            "displayName": "Username to run hadoop as",
                            "type": "string",
                            "description": "Username to run hadoop as"
                        },
                        {
                            "name": "hadoop/format",
                            "displayName": "Format HDFS",
                            "type": "string",
                            "description": "Format HDFS, Run \u0027hdfs namenode -format\u0027",
                            "default": "true"
                        },
                        {
                            "name": "hadoop/nn/public_ips",
                            "displayName": "Public ips for NameNodes",
                            "type": "array",
                            "description": "Public ips of Namenodes",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/nn/private_ips",
                            "displayName": "Public ips for NameNodes",
                            "type": "array",
                            "description": "Public ips of Namenodes",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/rm/public_ips",
                            "displayName": "Public ips for ResourceManagers",
                            "type": "array",
                            "description": "Public ips of ResourceManagers",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/nn/private_ips",
                            "displayName": "Public ips for ResourceManagers",
                            "type": "array",
                            "description": "Public ips of ResourceManagers",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/public_ips",
                            "displayName": "Public ips for these nodes",
                            "type": "array",
                            "description": "Public ips of nodes in this group",
                            "default": "[10.0.2.15]"
                        },
                        {
                            "name": "hadoop/private_ips",
                            "displayName": "Private ips for these nodes",
                            "type": "array",
                            "description": "Private ips of nodes in this group",
                            "default": "[10.0.2.15]"
                        }
                    ]
                }

            ];

        return {

            // Getter Function of the Board Data Object.
            getBoardDataObject: function () {
                return boardDataObject;
            },


            // Get the Updated BoardDataObject.
            getUpdatedBoardDataObject: function () {
                return updatedBoardObject;
            },

            // Setter Function.
            setBoardDataObject: function (dataObject) {
                $log.info("Inside the set data object service");
                boardDataObject = dataObject;
            },

            //Get dummy cookbook meta data json.
            getCookbookMetaData: function () {
                return cookbookMetaData;
            }

        };


    }]);



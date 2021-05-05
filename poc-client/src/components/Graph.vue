<template>
    <div>
        <button @click="loadGraph">Reload Graph</button>
        {{ graphMessage }}
        <Network 
            ref="network"
            id="graph"
            :nodes="nodes"
            :edges="edges"
            :options="options"
        />

        <button class="toggle-form-btn" v-for="k in Object.keys(forms)" :key="k" @click="toggleForm(k)">{{ k }}</button>

        <FormCreate
            title="Add Permission"
            v-if="forms['permission']"
            :callback="loadGraph"
            post-to="http://localhost:8080/permissions"
            :fields="['id', 'code']"
        />

        <FormCreate 
            title="Add Security Group"
            v-if="forms['securityGroup']"
            :callback="loadGraph"
            post-to="http://localhost:8080/security-groups"
            :fields="['id', 'code']"
        />

        <FormCreate 
            title="Add User"
            v-if="forms['addUser']"
            :callback="loadGraph"
            post-to="http://localhost:8080/user"
            :fields="['id', 'name', 'marketplaceId']"
        />

        <FormRelate 
            title="Add Permission to Security Group"
            v-if="forms['addPermissionToSecurityGroup']"
            :callback="loadGraph"
            post-to="http://localhost:8080/security-groups/add-permission"
            :options1="nodes.filter(f => f.group == 'SecurityGroup')"
            :options2="nodes.filter(f => f.group == 'Permission')"
            :mountBody="(op1, op2) => ({toGivePermissionId: op1, permissionId: op2})"
        />

        <FormRelate 
            title="Add Security Group to Security Group"
            v-if="forms['addSecurityGroupToSecurityGroup']"
            :callback="loadGraph"
            post-to="http://localhost:8080/security-groups/add-security-group"
            :options1="nodes.filter(f => f.group == 'SecurityGroup')"
            :options2="nodes.filter(f => f.group == 'SecurityGroup')"
            :mountBody="(op1, op2) => ({toGiveSecurityGroupId: op1, securityGroupId: op2})"
        />

        <FormRelate 
            title="Add Permission to User"
            v-if="forms['addPermissionToUser']"
            :callback="loadGraph"
            post-to="http://localhost:8080/user/add-permission"
            :options1="nodes.filter(f => f.group == 'User')"
            :options2="nodes.filter(f => f.group == 'Permission')"
            :mountBody="(op1, op2) => ({toGivePermissionId: op1, permissionId: op2})"
        />

        <FormRelate 
            title="Add Security Group to User"
            v-if="forms['addSecurityGroupToUser']"
            :callback="loadGraph"
            post-to="http://localhost:8080/user/add-security-group"
            :options1="nodes.filter(f => f.group == 'User')"
            :options2="nodes.filter(f => f.group == 'SecurityGroup')"
            :mountBody="(op1, op2) => ({toGiveSecurityGroupId: op1, securityGroupId: op2})"
        />

        <FormQuery 
            title="Query Users Permission"
            v-if="forms['queryUserPermissions']"
            :callback="markPermissions"
            get-to="http://localhost:8080/user/{0}/permissions"
            :options1="nodes.filter(f => f.group == 'User')"
        />
    </div>
</template>

<script>

import axios from 'axios'
import { Network } from 'vue2vis'
import FormCreate from './FormCreate'
import FormRelate from './FormRelate'
import FormQuery from './FormQuery'

export default {
    components: {
        Network,
        FormCreate,
        FormRelate,
        FormQuery
    },
    data() {
        return {
            graphMessage: '',
            forms: {
                permission: true,
                securityGroup: false,
                addUser: false,
                addPermissionToSecurityGroup: false,
                addPermissionToUser: false,
                addSecurityGroupToUser: false,
                addSecurityGroupToSecurityGroup: false,
                queryUserPermissions: false
            },
            nodes: [],
            edges: [],
            options: {
                nodes: {
                    shape: 'dot'
                },

                edges: {
                    arrows: 'to',
                    smooth: {
                        forceDirection: "none",
                        roundness: 0.55
                        }
                },
                physics: {
                    "barnesHut": {
                    "centralGravity": 0.15,
                    "springLength": 285
                    },
                    "minVelocity": 0.75
                },
                groups: {
                    Permission: { 
                        color: {
                            background: '#90a358',
                            highlight: 'yellow'
                        }
                    },
                    SecurityGroup: {
                        color: {
                            background: '#587da3',
                            highlight: 'yellow'
                        }
                    },
                    User: { 
                        color: {
                            background: '#d15a5a',
                            highlight: 'yellow'
                        }
                    },
                }
            }
        }
    },
    mounted() {
        this.loadGraph()
    },
    methods: {
        loadGraph() {
            this.graphMessage = 'Loading graph...'
            axios.get(`http://localhost:9090/graph/get-graph`)
                .then(response => {
                    this.nodes = response.data.nodes.map(n => ({
                        ...n,
                        label: n.label.join(' - ')
                    }))
                    this.edges = response.data.edges
                    this.graphMessage = 'Graph loaded successfully!'
                })
                .catch(error => {
                    this.graphMessage = 'Error loading graph'
                })
        },
        toggleForm(f) {
            Object.keys(this.forms).forEach(k => {
                this.forms[k] = false
            });
            this.forms[f] = true
        },
        markPermissions(data) {
            this.$refs.network.setSelection({
                nodes: data.map(n => n.id)
            })
        }
    }
}
</script>

<style scoped>


#graph {
    width: 100%;
    height: 600px;
    border: 1px solid #ccc;
}

.toggle-form-btn {
    margin: 3px 5px;
}





</style>
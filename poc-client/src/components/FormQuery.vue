<template>
    <div>
        <h3>{{title}}</h3>

        <select v-model="op1">
            <option disabled value="">Please select one</option>
            <option v-for="o in options1" :key="o.id" :value="o.id">{{ o.label }}</option>
        </select>

        <button @click="post">Query</button><br />
        {{ msg }}
    </div>
</template>

<script>

import axios from 'axios'

export default {
    props: {
        title: {
            type: String, required: true
        },
        options1: {
            type: Array, required: true
        },
        getTo: {
            type: String, required: true
        },
        callback: {
            type: Function, required: false
        }
    },
    data() {
        return {
            op1: '',
            msg: ''
        }
    },
    methods: {
        post() {
            this.msg = 'Querying...'
            axios.get(this.getTo.replace('{0}', this.op1))
            .then(response => {
                this.model = {};
                if (this.callback)
                    this.callback(response.data);
                this.msg = 'Executed!'
            })
            .catch(error => {
                this.msg = 'Error executing. Try again.' + error
            })
        }
    }
}
</script>

<style scoped>

select {
    display: block;
    margin: 3px auto;
}

</style>
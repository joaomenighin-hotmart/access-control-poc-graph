<template>
    <div>
        <h3>{{title}}</h3>

        <select v-model="op1">
            <option disabled value="">Please select one</option>
            <option v-for="o in options1" :key="o.id" :value="o.id">{{ o.label }}</option>
        </select>

        <select v-model="op2">
            <option disabled value="">Please select one</option>
            <option v-for="o in options2" :key="o.id" :value="o.id">{{ o.label }}</option>
        </select>

        <button @click="post">Salvar</button><br/>
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
        options2: {
            type: Array, required: true
        },
        postTo: {
            type: String, required: true
        },
        mountBody: {
            type: Function, required: true
        },
        callback: {
            type: Function, required: false
        }
    },
    data() {
        return {
            op1: '',
            op2: '',
            msg: ''
        }
    },
    methods: {
        post() {
            this.msg = 'Saving...'
            axios.post(this.postTo, this.mountBody(this.op1, this.op2))
            .then(response => {
                this.model = {};
                if (this.callback)
                    this.callback();
                this.msg = 'Saved successfully!'
            })
            .catch(error => {
                this.msg = 'Error saving. Try again.'
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
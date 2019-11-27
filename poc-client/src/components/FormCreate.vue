<template>
    <div>
        <h3>{{title}}</h3>
        <input v-for="(f, i) in fields" :key="`f-${i}`" v-model="model[f]" :placeholder="f" type="text" />
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
        fields: {
            type: Array, required: true
        },
        postTo: {
            type: String, required: true
        },
        callback: {
            type: Function, required: false
        }
    },
    data() {
        return {
            model: {},
            msg: ''
        }
    },
    methods: {
        post() {
            this.msg = 'Saving...'
            axios.post(this.postTo, this.model)
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

input[type='text'] {
    display: block;
    margin: 3px auto;
}

</style>
const app = Vue.createApp({
    data() {
        return {
            products: []
        };
    },
    mounted() {
        axios.get('/api/products')
        .then(res => { this.products = res.data; });
    },
    methods: {
        
        async deleteProduct(id) {
            if (!confirm("本当に削除しますか？")) return;

            try {
                await axios.delete(`/api/products/${id}`);
                alert("削除しました");
                this.products = this.products.filter(p => p.id !== id);
            } catch (error) {
                if (error.response && error.response.status === 404) {
                    alert("商品が見つかりません（404）");
                } else {
                    alert("削除に失敗しました");
                }
            }
        }
    }
});
app.mount('#app');



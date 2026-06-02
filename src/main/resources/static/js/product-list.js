const app = Vue.createApp({
    data() {
        return {
            products: []
        };
    },
    mounted() {
        axios.get('/api/products/list-by-maker')
        .then(res => { this.products = res.data; });
    },
    methods: {
        
        async deleteDetail(id) {
            if (!confirm("本当に削除しますか？")) return;

            try {
                await axios.delete(`/api/stock-detail/${id}`);
                alert("削除しました");
                const res = await axios.get('/api/products/list-by-maker');
                this.products = res.data;
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



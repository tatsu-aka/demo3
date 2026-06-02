const app = Vue.createApp({
    data() {
        return {
            stocks: [],
            detail: [],      // 内訳データ
            showModal: false // モーダル表示フラグ
        };
    },

    mounted() {
        this.loadStocks();
    },

    methods: {
        //在庫一覧
        loadStocks() {
            axios.get("/api/products/list-by-maker")
                .then(res => {
                    this.stocks = res.data;
                })
                .catch(err => console.error(err));
        },

        // 内訳取得
        showDetail(productId) {
            axios.get(`/api/stock-detail/${productId}`)
                .then(res => {
                    this.detail = res.data;
                    this.showModal = true;
                })
                .catch(err => console.error(err));
        },

        async deleteDetail(id) {
            if (!confirm("本当に削除しますか？")) return;

            try {
                await axios.delete(`/api/stock-detail/${id}`);

                // ★ 一覧を再取得
                const res = await axios.get("/api/products/list-by-maker");
                this.stocks = res.data;

                alert("削除しました");
            } catch (error) {
                alert("削除に失敗しました");
            }
        }
    }
});

app.mount("#app");
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
            axios.get("/api/stock/summary")
                .then(res => {
                    this.stocks = res.data;
                })
                .catch(err => console.error(err));
        },

        // 内訳取得
        showDetail(productName) {
            axios.get(`/api/stock/summary/maker/${productName}`)
                .then(res => {
                    this.detail = res.data;
                    this.showModal = true;
                })
                .catch(err => console.error(err));
        }
    }
});

app.mount("#app");
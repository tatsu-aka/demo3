const app = Vue.createApp({
    data() {
        return {
            products: [],
            selectedProductId: "",
            history: [],
            productName: "",
            makerName: ""
        };
    },

    mounted() {
        this.loadProducts();
    },

    methods: {
        async loadProducts() {
            try {
                const res = await axios.get("/api/products");
                this.products = res.data;
            } catch (e) {
                console.error(e);
                alert("商品一覧の取得に失敗しました")
            }
        },

        async loadHistory() {
            if(!this.selectedProductId) {
                this.history = [];
                return;
            }
            try {
                const res = await axios.get(`/api/products/${this.selectedProductId}/price-history`);
                this.history = res.data;

                if (this.history.length > 0) {
                    this.productName = this.history[0].productName;
                    this.makerName = this.history[0].makerName;
                }
            } catch (e) {
                console.error(e);
                alert("価格履歴取得に失敗しました")
            }
        }
    }
}).mount("#app");
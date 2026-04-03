const app = {
    data() {
        return {
            products: [],
            productId: "",
            newCostPrice: "",
            startDate: "",
            errors: {},
            successMessage: ""
        };
    },

    mounted() {
        // 商品一覧を取得
        axios.get("/api/products")
            .then(res => {
                this.products = res.data;
            })
            .catch(err => {
                console.error("商品一覧取得エラー:", err);
            });
    },

    methods: {
        // バリデーション
        validate() {
            this.errors = {};

            if (!this.productId) {
                this.errors.productId = "商品を選択してください";
            }

            if (!this.newCostPrice) {
                this.errors.newCostPrice = "原価を入力してください";
            } else if (this.newCostPrice <= 0) {
                this.errors.newCostPrice = "原価は1以上で入力してください";
            }

            if (!this.startDate) {
                this.errors.startDate = "適用開始日を入力してください";
            }

            return Object.keys(this.errors).length === 0;
        },

        // 価格変更処理
        async changePrice() {
            if (!this.validate()) {
                return;
            }

            try {
                await axios.post(`/api/products/${this.productId}/price`, {
                    newCostPrice: Number(this.newCostPrice),
                    startDate: this.startDate
                });

                this.successMessage = "価格を変更しました！";

                // 入力欄クリア
                this.newCostPrice = "";
                this.startDate = "";

            } catch (error) {
                console.error("価格変更エラー:", error);

                if (error.response && error.response.data) {
                    alert("サーバーエラー: " + JSON.stringify(error.response.data));
                } else {
                    alert("価格変更に失敗しました");
                }
            }
        }
    }
};

Vue.createApp(app).mount("#app");

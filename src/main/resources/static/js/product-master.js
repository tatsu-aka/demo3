const app = Vue.createApp({
    data() {
        return {
            products: [],        // 商品一覧
            categories: [],      // カテゴリマスタ
            units: [],           // 単位マスタ
            makers: [],          // メーカーマスタ

            // フォーム
            form: {
                id: null,
                name: "",
                category: "",
                unit: "",
                stock: 0,
                makerId: null
            }
        };
    },

    async mounted() {
        await this.loadMaster();
        await this.loadProducts();
    },

    methods: {
        // -------------------------
        // マスタ取得
        // -------------------------
        async loadMaster() {
            const [catRes, unitRes, makerRes] = await Promise.all([
                axios.get("/api/master/categories"),
                axios.get("/api/master/units"),
                axios.get("/api/master/makers")
            ]);

            this.categories = catRes.data;
            this.units = unitRes.data;
            this.makers = makerRes.data;
        },

        // -------------------------
        // 商品一覧取得
        // -------------------------
        async loadProducts() {
            const res = await axios.get("/api/products/master");
            this.products = res.data;
        },

        // -------------------------
        // 編集ボタン
        // -------------------------
        edit(p) {
            this.form = {
                id: p.id,
                name: p.name,
                category: p.category,
                unit: p.unit,
                makerId: p.maker ? p.maker.id : null
            };
        },

        // -------------------------
        // 保存（新規 or 更新）
        // -------------------------
        async save() {
            const payload = {
                name: this.form.name,
                category: this.form.category,
                unit: this.form.unit,
                stock: this.form.stock,
                maker: { id: this.form.makerId }
            };

            if (this.form.id) {
                // 更新
                await axios.put(`/api/products/master/${this.form.id}`, payload);
            } else {
                // 新規
                await axios.post(`/api/products/master`, payload);
            }

            await this.loadProducts();
            this.resetForm();
        },

        // -------------------------
        // 削除
        // -------------------------
        async remove(id) {
            if (!confirm("削除しますか？")) return;

            await axios.delete(`/api/products/master/${id}`);
            await this.loadProducts();
        },

        // -------------------------
        // フォームクリア
        // -------------------------
        resetForm() {
            this.form = {
                id: null,
                name: "",
                category: "",
                unit: "",
                makerId: null
            };
        }
    }
});

app.mount("#app");

const app = Vue.createApp({
    data() {
        return {
            products: [],//商品一覧
            productId: "",//選択された商品ID
            history: []
        };
    },

    mounted() {
        //ページ表示時に商品一覧取得
        this.loadProducts();
    },

    methods: {
        //商品一覧取得
        async loadProducts() {
            const res = await fetch("/api/products");
            this.products = await res.json();
             
        },

        //商品が選ばれたら在庫推移データを取得
        async loadData() {
            if (!this.productId) return;

            const res = await fetch(`/api/stock-history/${this.productId}`);
            this.history = await res.json();

            this.drawChart(); //データ取得後グラフ描画
        },

        //Echartsでグラフ描画
        drawChart() {
            const chartDom = document.getElementById('chart');
            const chart = echarts.init(chartDom);

            const option = {
                title: {
                    text: '在庫推移',
                    left: 'center'
                },
                tooltip: {
                    trigger: 'axis'
                },
                xAxis: {
                    type: 'category',
                    data: this.history.map(h => h.dateTime) //日付
                },
                yAxis: {
                    type: 'value'
                },
                series: [{
                    data: this.history.map(h => h.stock), //在庫数
                    type: 'line',
                    smooth: true,
                    areaStyle: {} //塗りつぶし
                }]
            };

            chart.setOption(option);
        }
    }
});
app.mount("#app");
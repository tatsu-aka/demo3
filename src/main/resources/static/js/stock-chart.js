const app = Vue.createApp({
    data() {
        return {
            products: [],//商品一覧
            productId: "",//選択された商品ID
            history: [],
            chartType: "line"
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
        toggleChart() {
            this.chartType = this.chartType === "line" ? "bar" : "line";
            this.drawChart();
        },

        //商品が選ばれたら在庫推移データを取得
        async loadData() {
            if (!this.productId) return;

            const res = await fetch(`/api/stock-history/${this.productId}`);
            const data = await res.json();

            //日付フォーマット
            const formatted = data.map(item => {
                const date = new Date(item.dateTime);

                const month = date.getMonth() + 1;
                const day = date.getDate();
                const hour = date.getHours();
                const minute = date.getMinutes().toString().padStart(2, "0");

                return {
                    dateTime: `${month}/${day} ${hour}:${minute}`, stock: item.stock
                };
            });
            this.history = formatted;

            await Vue.nextTick();

            this.drawChart(); //データ取得後グラフ描画
        },

        //Echartsでグラフ描画
        drawChart() {
            const chartDom = document.getElementById('chart');

            let oldChart = echarts.getInstanceByDom(chartDom);
            if (oldChart) {
                oldChart.dispose();
            }
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
                    boundaryGap: false,
                    axisLabel: {
                        formatter: value => value,
                        overflow: 'truncate',
                        width: 80
                    },
                    data: this.history.map(h => String(h.dateTime)) //日付
                },
                yAxis: {
                    type: 'value'
                },
                series: [{
                    data: this.history.map(h => h.stock), //在庫数
                    type: this.chartType,
                    smooth: this.chartType === "line",
                    areaStyle: this.chartType === "line" ? {} : null, //塗りつぶし
                    barWidth: this.chartType === "bar" ? 20 : null
                }]
            };

            chart.setOption(option);
        }
    }
});
app.mount("#app");
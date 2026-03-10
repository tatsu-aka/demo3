app.mixin({
    
    mounted() {
        //ページ表示時に商品一覧取得
        this.loadProducts();
    },

    methods: {
        //商品一覧読み込み
        async loadProducts() {
            const res = await fetch("/api/products");
            this.products = await res.json();
        },

        async loadOutData() {
            if(!this.productId) return;

            const res = await fetch(`/api/stock-history/out/${this.productId}`);
            const data = await res.json();

            const formatted = data.map(item => {
                const date = new Date(item.dateTime);
                const month = date.getMonth() + 1;
                const day = date.getDate();
                const hour = date.getHours();
                const minute = date.getMinutes().toString().padStart(2, "0");

                return {
                    dateTime: `${month}/${day} ${hour}:${minute}`,
                    quantity: item.quantity
                };
            });

            this.outHistory = formatted;

            await Vue.nextTick();
            this.drawOutChart();
        },

        //出庫推移グラフ描画
        drawOutChart() {
            const chartDom = document.getElementById("outChart");

            let old = echarts.getInstanceByDom(chartDom);
            if (old) old.dispose();

            const chart = echarts.init(chartDom);

            const option = {
                title: {
                    text: '出庫推移',
                    left: 'center'
                },
                tooltip: { trigger: 'axis' },
                xAxis: {
                    type: 'category',
                    boundaryGap: true,
                    data: this.outHistory.map(h => h.dateTime)
                },

                yAxis: { type: 'value' },
                series: [{
                    data: this.outHistory.map(h => h.quantity),
                    type: 'bar',
                    barWidth: 20,
                    itemStyle: {
                        color: '#F44336'
                    }
                }]
            };
            chart.setOption(option);
        }
    }
});
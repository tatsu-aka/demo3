const app = Vue.createApp({
    data() {
        return {
            productId: "",
            products: [],
            inHistory: [],
            outHistory: []
        };
    },

    mounted() {
        this.loadProducts();
    },

    methods: {
        //商品一覧読み込み
        async loadProducts() {
            const res = await fetch("/api/products");
            this.products = await res.json();
        },

        async loadInData() {
            if(!this.productId) return;

            const res = await fetch(`/api/stock-history/in/${this.productId}`);
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
            this.inHistory = formatted;

            await Vue.nextTick();
            this.drawInChart();
        },

        //入庫推移グラフ
        drawInChart() {
            const chartDom = document.getElementById("inChart");

            let old = echarts.getInstanceByDom(chartDom);
            if (old) old.dispose();

            const chart = echarts.init(chartDom);

            const option = {
                title: {
                    Text: '入庫推移',
                    left: 'center'
                },
                tooltip: { trigger: 'axis' },
                xAxis: {
                    type: 'category',
                    boundaryGap: true,
                    data: this.inHistory.map(h => h.dateTime)
                },
                yAxis: { type: 'value' },
                series: [{
                    data: this.inHistory.map(h => h.quantity),
                    type: 'bar',
                    barWidth: 20,
                    itemStyle: {
                        color: '#4CAF50'
                    }
                }]
            };

            chart.setOption(option);
        }
    }
});
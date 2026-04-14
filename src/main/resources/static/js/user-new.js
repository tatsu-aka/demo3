const app = Vue.createApp({
    data() {
        return {
            username: "",
            password: "",
            passwordConfirm: "",
            role: "USER",
            errorMessage: "",
            successMessage: "",
        };
    },

    methods: {
        async registerUser() {

            //入力チェック
            if (!this.username || !this.password || !this.passwordConfirm) {
                this.errorMessage = "すべての項目を入力してください"
                return;
            }

            if (this.password !== this.passwordConfirm) {
                this.errorMessage = "パスワードが一致しません"
                return;
            }

            this.errorMessage = "";
            this.successMessage = "";

            try {
                const res = await axios.post("/api/users", {
                    username: this.username,
                    password: this.password,
                    role: this.role
                });

                this.successMessage = "ユーザーを登録しました。3秒後に商品一覧へ戻ります。"

                setTimeout(() => {
                    window.location.href = "/product";
                }, 3000);
            } catch (e) {
                this.errorMessage = "登録に失敗しました。すでに存在するユーザー名の可能性があります。"
            }
        }
    }
});

app.mount("#app");
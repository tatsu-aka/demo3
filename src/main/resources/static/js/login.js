const app = Vue.createApp({
    data() {
        return {
            username: "",
            password: "",
            errorMessage: ""
        };
    },

    methods: {
        validateForm(event) {
            this.errorMessage = "";

            if (this.username.trim() === "" || this.password.trim() === "") {
                event.preventDefault();
                this.errorMessage = "ユーザー名とパスワードを入力してください"
                return;
            }

            if (this.password.length < 4) {
                event.preventDefault();
                this.errorMessage = "パスワードは4文字以上で入力してください"
                return;
            }
        }
    }
}).mount("#app");
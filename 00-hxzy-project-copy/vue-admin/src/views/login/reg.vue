<template>
  <div>
    <el-card class="login-form-layout">
      <el-form autoComplete="on"
               :model="loginForm"
               :rules="loginRules"
               ref="loginForm"
               label-position="left">
        <div style="text-align: center">
          <svg-icon icon-class="login-mall" style="width: 56px;height: 56px;color: #409EFF"></svg-icon>
        </div>
        <h2 class="login-title color-main"> 商城 注册</h2>
        <el-form-item prop="username">
          <el-input name="username"
                    type="text"
                    v-model="loginForm.username"
                    autoComplete="on"
                    placeholder="请输入手机号">
          <span slot="prefix">
            <svg-icon icon-class="user" class="color-main"></svg-icon>
          </span>
              <span slot="suffix" @click="codes">发送验证码
              <svg-icon icon-class="eye" class="color-main"></svg-icon>
            </span>
          </el-input>
        </el-form-item>
        <el-form-item prop="username">
          <el-input name="username"
                    type="text"
                    v-model="loginForm.code"
                    autoComplete="on"
                    placeholder="请输入验证码">
          <span slot="prefix">
            <svg-icon icon-class="user" class="color-main"></svg-icon>
          </span>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input name="password"
                    :type="pwdType"
                    @keyup.enter.native="handleLogin"
                    v-model="loginForm.password"
                    autoComplete="on"
                    placeholder="请输入密码">
          <span slot="prefix">
            <svg-icon icon-class="password" class="color-main"></svg-icon>
          </span>
            <span slot="suffix" @click="showPwd">
            <svg-icon icon-class="eye" class="color-main"></svg-icon>
          </span>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input name="password"
                    :type="pwdType"
                    @keyup.enter.native="handleLogin"
                    v-model="loginForm.confimpassword"
                    autoComplete="on"
                    placeholder="请再次输入密码">
          <span slot="prefix">
            <svg-icon icon-class="password" class="color-main"></svg-icon>
          </span>
            <span slot="suffix" @click="showPwd">
            <svg-icon icon-class="eye" class="color-main"></svg-icon>
          </span>
          </el-input>
        </el-form-item>
        <el-form-item style="margin-bottom: 10px">
          <el-button style="width: 100%" type="primary"  @click.native.prevent="handleReg">
            注册
          </el-button>
        </el-form-item>
        <el-form-item style="margin-bottom: 10px">
          <el-button style="width: 100%" type="primary"  @click.native.prevent="handleLogin">
            登录
          </el-button>
        </el-form-item>
        <el-form-item style="margin-bottom: 10px">
          <el-button style="width: 100%" type="primary"  @click.native.prevent="handleUrl">
            github下载
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <img :src="login_center_bg" class="login-center-layout">
  </div>
</template>

<script>
  import {isvalidUsername} from '@/utils/validate';
  import login_center_bg from '@/assets/images/login_center_bg.png'
  import { reg, codes, getInfo } from '@/api/login'
  export default {
    name: 'login',
    data() {
      const validateUsername = (rule, value, callback) => {
        if (!isvalidUsername(value)) {
          callback(new Error('请输入正确的用户名'))
        } else {
          callback()
        }
      };
      const validatePass = (rule, value, callback) => {
        if (value.length < 3) {
          callback(new Error('密码不能小于3位'))
        } else {
          callback()
        }
      };
      return {
        loginForm: {
          username: '',
            code:'',
            confimpassword:'',
          password: ''
        },
        loginRules: {
          username: [{required: true, trigger: 'blur', validator: validateUsername}],
          password: [{required: true, trigger: 'blur', validator: validatePass}]
        },
        loading: false,
        pwdType: 'password',
        login_center_bg
      }
    },
    methods: {
      showPwd() {
        if (this.pwdType === 'password') {
          this.pwdType = ''
        } else {
          this.pwdType = 'password'
        }
      },
      handleLogin() {
          this.$router.push({path: '/login'})
      },
        handleReg() {
            this.$refs.loginForm.validate(valid => {
                if (valid) {

                    reg(this.loginForm.username, this.loginForm.password ,this.loginForm.code,this.loginForm.confimpassword).then(response => {

                        this.$router.push({path: '/login'})
                    }).catch(error => {
                        console.log(error)
                    })
                } else {
                    console.log('参数验证不合法！');
                    return false
                }
            })
        },
        codes() {
            if (this.loginForm.username ==null ||this.loginForm.username =='' || !isvalidUsername(this.loginForm.username)) {
               alert('请输入正确的手机号');
            } else {
                codes(this.loginForm.username).then(response => {
alert('发送成功');
                }).catch(error => {
                    console.log(error)
                })
            }
        },
        handleUrl() {
           window.location='https://gitee.com/zscat/mall';
        }
    }
  }
</script>

<style scoped>
  .login-form-layout {
    position: absolute;
    left: 0;
    right: 0;
    width: 360px;
    margin: 140px auto;
    border-top: 10px solid #409EFF;
  }

  .login-title {
    text-align: center;
  }

  .login-center-layout {
    background: #409EFF;
    width: auto;
    height: auto;
    max-width: 100%;
    max-height: 100%;
    margin-top: 200px;
  }
</style>

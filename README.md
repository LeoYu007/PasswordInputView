 ## 一个支持多种风格的自定义的密码(验证码)输入框

 ![image](https://github.com/LambertCoding/PasswordInputView/blob/master/image/password_input_view2.png)

## Features

* 仿支付宝微信风格
* 下划线风格
* 可显示明文或者密文，密文支持显示圆点，星号，或者任意字符
* 设置密码框之间的间隔和圆角(间隔为0时圆角只显示最左和最右的圆角)
* 设置边框和密码的颜色
* 支持已输入部分单独设置颜色

### [关于原理](https://juejin.im/post/5d121b92e51d45108f254230)

## Usages

### 只有一个类，直接拷贝到你的项目中，别忘了自定义属性的配置

#### 在布局中直接使用
```xml
    <!--
         注意EditText的默认背景会有边距，会影响绘制，所以需要去掉
         android:background="@null"
     -->
    <com.matthew.passwordinput.lib.PasswordInputView
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_marginTop="10dp"
        android:background="@null"
        android:padding="1dp"
        android:text="123"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:pwv_haveInputBorderColor="@color/colorAccent"
        app:pwv_pwdStyle="plaintext"
        app:pwv_radius="10dp"
        app:pwv_spacing="12dp" />

```
#### 设置监听器
```java
    passwordView.setInputListener(new PasswordInputView.InputListener() {
        @Override
        public void onInputCompleted(String text) {
            // 输入完成后回调
        }
    });
```
#### 属性参考
```xml
    <declare-styleable name="PasswordInputView">
        <attr name="pwv_maxLength" format="integer" /> // 最大长度

        <attr name="pwv_borderColor" format="color" /> // 边框颜色
        <attr name="pwv_pwdColor" format="color" /> // 密码颜色
        <attr name="pwv_haveInputBorderColor" format="color" /> // 已输入部分边框的颜色

        <attr name="pwv_strokeWidth" format="dimension" /> // 边框宽度
        <attr name="pwv_radius" format="dimension" /> // 圆角半径
        <attr name="pwv_spacing" format="dimension" /> // 每个密码框之间的间距
        <attr name="pwv_asterisk" format="string" />
        // 当密码风格为星号风格时，可以用任意字符替换星号，替换的字符为pwv_asterisk的第一个字符

        <attr name="pwv_borderStyle" format="enum"> // 边框风格 方框 和 下划线
            <enum name="box" value="0" />
            <enum name="line" value="1" />
        </attr>
        <attr name="pwv_pwdStyle" format="enum"> // 密码风格 圆点、星号、明文
            <enum name="circle" value="0" />
            <enum name="asterisk" value="1" />
            <enum name="plaintext" value="2" />
        </attr>
    </declare-styleable>
```
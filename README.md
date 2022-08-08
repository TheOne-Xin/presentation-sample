# 背景：
日常生活中，有时候会遇到 Android 设备连接两个屏幕进行显示的问题，比如酒店登记信息时，一个屏幕用于员工操作，一个屏幕显示相关信息供顾客查看。这里就涉及到 Android 的双屏异显的问题，实现 Android 的双屏异显，Google 也提供了相应的 API 方法 Presentation。
# 1 Presentation 介绍
要了解 API 的具体调用，推荐先查看官方的文档：[Presentation文档](https://developer.android.com/reference/android/app/Presentation)
Android 从4.2开始支持双屏显示，开发时需 minSdkVersion >= 17 。Android 连接两个屏幕时，自动分配主屏和副屏，主屏显示正常的 Activity 界面，副屏通过创建 Presentation 类来实现。
通过查看 Presentation 继承关系可知，Presentation 继承自 Dialog，创建的时候需要遵循 Dialog 相关要求。当和 Presentation 相关联的屏幕被移除后，Presentation 也会自动的被移除，所以当 Activity 处于 pause 和 resume 的状态时，Presentation 也需要特别注意当前显示的内容的状态。
# 2 创建 Presentation
```java
public class MyPresentation extends Presentation {
    private Context context;
    private Display display;

    public MyPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        this.context = outerContext;
        this.display = display;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_presentation);

        initView();
    }

    private void initView() {
        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
```
创建 Presentation 后，自动生成构造函数，构造函数中
- outerContext：上下文环境，可以是主屏 Activity，ApplicationContext 或者 Service
- display：副屏的 Display

OnCreate 方法中完成布局的初始化，可设置相应按钮的监听，关闭当前 Presentation，执行 dismiss() 方法即可（前提：副屏支持点击）
# 3 获取屏幕 Display 信息
创建好 Presentation 后，需要在主屏 Activity 上获取屏幕的 Display 信息，让其显示副屏信息，Android 系统提供了两个方式来获取 Display 信息。
## 3.1 MediaRouter 方式
```java
MediaRouter mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);
if (route != null) {
    Display presentationDisplay = route.getPresentationDisplay();
    if (presentationDisplay != null) {
        MyPresentation myPresentation = new MyPresentation(MainActivity.this, presentationDisplay);
        myPresentation.show();
    }
}
```
## 3.2 DisplayManager 方式
```java
DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
Display[] displays = mDisplayManager.getDisplays();
if (displays.length > 1) {
   //displays[0] 主屏，displays[1] 副屏
   MyPresentation myPresentation = new MyPresentation(MainActivity.this, displays[1]);
   myPresentation.show();
}
```
在 Activity 中添加上面的代码后，即可实现双屏双显的效果。
# 4 双屏双显的优化：
## 4.1 副屏显示 Toast 提示
通过上面的方法实现双屏双显后，如果在Presentation创建 Toast 提示，会出现提示显示在主屏上的问题，这里需要注意创建 Toast 的 Context 参数。
```java
Toast.makeText(getContext(),"副屏Toast",Toast.LENGTH_SHORT).show();
```
## 4.2 副屏内容常驻，不退出
因为 Presentation 相当于在主屏的 Activity 上创建了一个特殊 Dialog，所以 Presentation 会随着主屏 Activity 的生命周期显示隐藏，如何让副屏常驻，不随主屏 Activity 退出。在 Dialog 中，我们知道可以通过创建系统级弹框的方式来做，Presentation 中也是一样。
- 添加系统权限
```java
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
```
- 在 Presentation 中添加系统弹框代码
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
} else {
    getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
}
```
Android8.0 及以上的系统只能使用 TYPE_APPLICATION_OVERLAY 窗口类型来创建悬浮窗。
# 5 Presentation 限制
- Presentation 实际上是一个特殊的 Dialog，因此在 Presentation 中无法创建 Fragment、Popupwindow 等组件。
- Presentation 显示的副屏和主屏的尺寸是不相同的，绘制 UI 时需特别注意。
# 6 调试
如果没有多屏设备也可以使用模拟器，或者普通 Android 设备来进行双屏异显的调试。打开"设置-[开发者选项](https://developer.android.com/studio/debug/dev-options?hl=zh-cn)"界面，在列表中找到"模拟辅助显示设备"条目。点击后，在弹出的对话框中选择副屏的分辨率：

![simulate_secondary_displays](https://github.com/TheOne-Xin/presentation-sample/blob/master/images/simulate_secondary_displays.jpg)
每种分辨率都有安全和默认两种，安全模式会有一些限制，比如无法截屏。比如我们选中1080p，模拟器屏幕的左上角上会立即呈现出副屏的窗口，其显示内容默认为与主屏显示一致：

![secondary_displays_1080](https://github.com/TheOne-Xin/presentation-sample/blob/master/images/secondary_displays_1080.jpg)
副屏窗口就是一个 Dialog，可以拖动，让其显示在合适的位置。这里的副屏是不支持触摸动作的。
运行测试程序，效果如下：

![presentation_sample](https://github.com/TheOne-Xin/presentation-sample/blob/master/images/presentation_sample.jpg)

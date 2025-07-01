import usrFrameMain from '@/components/_frame/usr-frame-main.vue'
import usrFrameLogin from "@/components/_frame/usr-frame-login.vue"
import pageWelcome from "@/views/_frame/page-welcome.vue"
import usrFrameNotfound from "@/components/_frame/usr-frame-notfound.vue"
import usrFrameForbidden from "@/components/_frame/usr-frame-forbidden.vue";
import publicApproval from "@/views/public/public-approval.vue" // 添加公开审批页面引入
import segOnelink from "@/views/public/seg-onelink.vue" // 添加SEG ONELINK页面引入
import segAnalytics from "@/views/public/seg-analytics.vue" // 添加SEG ONELINK数据统计页面引入
import qaPage from "@/views/public/qa-page.vue" // 添加QA页面引入
import controlAsset from "@/views/other/control-asset.vue" // 添加电脑台账页面引入
import {createRouter, createWebHistory} from 'vue-router'
import NProgress from 'nprogress'
import loginUtil from "@/utils/LoginUtil";
import httpUtil from "@/utils/HttpUtil";
import {useRoutesStore} from "@/stores/_frame/routesStore";
import {useUserInfoStore} from "@/stores/_frame/userInfoStore";
import ObjectUtil from "@/utils/ObjectUtil";
import {useLoadRoutesStateStore} from "@/stores/_frame/loadRoutesStateStore";

// 定义路由
// 每个路由都需要映射到一个组件
const routes = [
    {
        name: 'gen',
        path: '/',
        component: usrFrameMain,
        redirect: '/welcome',
        children: [
            // 默认的静态页面，不需要动态添加
            {path: '/welcome', component: pageWelcome},
            {path: '/forbidden', component: usrFrameForbidden},
        ]
    },
    // login 页面
    {path: '/login', component: usrFrameLogin},
    // 公开页面
    {path: '/public-page', component: publicApproval},
    {path: '/seg-onelink', component: segOnelink}, // 添加SEG ONELINK路由
    {path: '/seg-analytics', component: segAnalytics}, // 添加SEG ONELINK数据统计路由
    {path: '/qa-page', component: qaPage}, // 添加QA页面路由
    // 404 Notfound
    // 理论上，这里应该永远匹配不到，因为路由守卫前置拦截会把将要访问的路由重定向的对应的路由上
    // 比如访问了`/aaa`一个不存在的路由，
    // 前置守卫会判断用户是否已经授权登录，如果没有会被重定向到`/login`；
    // 如果已经授权登录了，但该路由不在用户的权限内，会被重定向到`/forbidden`。
    // 只有一种情况例外：用户已经授权登录，且该路由在用户的权限内，但是该路由对应的组件不存在，这种情况下会渲染`usrFrameNotfound`组件
    {path: '/:pathMatch(.*)*', component: usrFrameNotfound},
]

/**
 * 创建一个路由实例
 * <br/>
 * Router文档地址：https://router.vuejs.org/zh/guide/advanced/composition-api.html
 */
const router = createRouter({
    // 我们在这里使用 hash 模式，它在URL后面使用了一个哈希字符（#），#后面的数据不会发往服务器
    // history: createWebHashHistory(),
    // 使用 history 模式，URL后没有一个哈希字符（#），但是这种模式要求服务器配置来支持
    history: createWebHistory(),
    routes, // `routes: routes` 的缩写
})

// 配置Router实例全局拦截器
// 全局前置路由守卫，每一次路由跳转前都进入这个 beforeEach 函数
router.beforeEach(async (to) => {
    // 开启页面进度条
    NProgress.start()

    // 获取授权状态
    let isAuth = loginUtil.isAuthenticated()

    // 添加不需要登录验证的白名单路径
    const whiteList = ['/login', '/register', '/forget-password', '/public-page', '/seg-onelink', '/seg-analytics', '/qa-page']
    
    // 判断是否在白名单中，如果是，直接放行
    if (whiteList.includes(to.path)) {
        return true
    }

    // 未授权，并且请求地址不是白名单，那么跳转到`/login`
    if (!isAuth) {
        return '/login'
    }
    // 已授权，但请求地址是`/login`，那么跳转到`/`
    if (isAuth && to.path === '/login') {
        return '/'
    }

    // 如果已经授过权，开始动态路由加载
    if (isAuth) {
        // 获取路由状态仓库
        let loadRoutesStateStore = useLoadRoutesStateStore()
        // 验证动态路由是否添加完成，如果没有，需要先同步添加路由
        if (!loadRoutesStateStore.hasLoadRoutes) {
            // 动态添加路由
            let result = await dynaAddRoute()

            if (result) {
                // 这里需要解释一下，
                // 如果你决定在导航守卫内部添加或删除路由，你不应该调用 router.replace()，而是通过 to.fullPath 返回新的位置来触发重定向，
                // 文档地址：https://router.vuejs.org/zh/guide/advanced/dynamic-routing.html#在导航守卫中添加路由
                return to.fullPath
            }

            // 返回false，中断当前请求
            return false
        }

        // 验证用户当前请求是否拥有路由的权限，如果没有，页面跳转到`/forbidden`
        if (!isPermThisRoute(to)) {
            return '/forbidden'
        }
    }
})

// 配置Router实例全局拦截器
// 全局后置路由守卫，每一次路由跳转后都进入这个 afterEach 函数
router.afterEach(() => {

    // 页面 loading 进度条结束
    NProgress.done();
})

/**
 * 动态添加路由
 *
 * @return true: 动态添加路由成功，false: 动态添加路由失败
 */
const dynaAddRoute = async () => {
    // 获取用户菜单缓存信息
    let userInfoStore = useUserInfoStore()
    let userMenus = userInfoStore.userMenus
    if (ObjectUtil.isEmptyObject(userMenus)) {
        await httpUtil.get('/userMenu').then(async res => {
            // 缓存用户菜单信息，避免每次请求服务器
            userMenus = res.data
            userInfoStore.userMenus = userMenus
        }).catch(err => {
            console.error(err)
        })
    }

    if (!ObjectUtil.isEmptyObject(userMenus)) {
        // 动态路由程序处理
        routesProcess(userMenus)
        return true;
    }

    return false;
}

/**
 * 整理routes信息，便于后续业务的使用
 * <p>
 * 构建的数据结构：<br>
 * <pre>
 * [
 *    {label:'', path:'', breadcrumb:[]},
 *    {label:'', path:'', breadcrumb:[]}
 *    ...
 * ]
 * </pre>
 * 构建的数据结构描述：
 * <p>
 * label: 用于在Tab上显示使用。<br>
 * path: 作为路由的标识，用于权限验证、定位Tab、定位左侧菜单选中。<br>
 * breadcrumb: 面包屑，通过遍历路由，由路由的`label`组成的数组。<br>
 *
 * @param userMenus 动态路由数据信息
 */
const routesProcess = (userMenus) => {
    // 系统所有路由
    let routesAll = []
    // 遍历菜单路由部分
    loopRoutes(routesAll, userMenus.menus)
    // 遍历其他零碎页面路由部分
    loopRoutes(routesAll, userMenus.pages)

    // 添加系统内置的静态路由部分到系统所有路由
    // Why? 为什么要添加静态页面部分，如果不添加，用户就没有权限访问/forbidden,/welcome的页面
    routesAll.push({
        label: '无权访问', path: '/forbidden', breadcrumb: ['无权访问页面']
    }, {
        label: 'Welcome', path: '/welcome', breadcrumb: ['控制台']
    })

    // 获取路由状态仓库
    let routesStore = useRoutesStore();
    // 保存`系统所有路由`到Stores中
    routesStore.routes = routesAll
    // 保存`菜单部分路由`到Stores中
    routesStore.menus = userMenus.menus

    // 动态添加路由
    addRoute(routesAll)

    let loadRoutesStateStore = useLoadRoutesStateStore()
    // 标记动态路由添加完成
    loadRoutesStateStore.hasLoadRoutes = true
}

/**
 * 递归遍历路由
 * <p>
 * 提取`系统所有路由`数据，及组装`breadcrumb`字段，
 * `breadcrumb`按照父类label、子类label、孙子类label构成。
 *
 * @param routesAll 系统所有路由
 * @param routes 路由数据集
 * @param breadcrumb 为路由新添加的面包屑字段
 */
const loopRoutes = (routesAll, routes, breadcrumb) => {
    for (let i = 0; i < routes.length; i++) {
        if (routes[i].children && routes[i].children.length > 0) {
            if (breadcrumb) {
                // [].concat(breadcrumb) 会返回一个新的数组
                // 避免了引用的存在
                routes[i]['breadcrumb'] = [].concat(breadcrumb);
            } else {
                routes[i]['breadcrumb'] = [];
            }
            routes[i].breadcrumb.push(routes[i].label);
            // 子节点集合递归继续遍历
            loopRoutes(routesAll, routes[i].children, routes[i].breadcrumb)
        } else {
            if (routes[i].path) {
                if (breadcrumb) {
                    // [].concat(breadcrumb) 会返回一个新的数组
                    // 避免了引用的存在
                    routes[i]['breadcrumb'] = [].concat(breadcrumb);
                } else {
                    routes[i]['breadcrumb'] = [];
                }
                routes[i].breadcrumb.push(routes[i].label);
                routesAll.push(routes[i])
            }
        }
    }
}

// 使用 Vite 的 import.meta.glob 方法动态导入所有位于 '@/views' 目录下的 .vue 文件
// 返回值是一个对象，键是文件路径，值是动态导入函数
// 例如：{ '/src/views/Home.vue': () => import('/src/views/Home.vue'), ... }
const modules = import.meta.glob('@/views/**/*.vue');

/**
 * 动态添加路由
 * <p>
 * router.addRoute() 挂载到路由系统中
 * <p>
 * 关于路由懒加载，文档地址：https://router.vuejs.org/zh/guide/advanced/lazy-loading.html
 * @param routesAll 系统所有路由
 */
const addRoute = (routesAll) => {
    for (let i = 0; i < routesAll.length; i++) {
        let route = routesAll[i];
        // 过滤掉内置的静态路由
        if (route.path === '/forbidden' || route.path === '/welcome') {
            continue;
        }

        // 检查 component 是否存在
        if (!route.component) {
            console.error(`Component path is missing for route: ${route.path}`);
            continue;
        }

        // 检查路由是否已经存在
        if (!router.hasRoute(route.path)) {
            // 添加子路由，子路由挂载在`gen`路由下
            router.addRoute('gen', {
                path: route.path,
                // 路由懒加载
                component: modules[`/src/views${route.component.startsWith('/') ? route.component : `/${route.component}`}.vue`],
            });
        }
    }
};

/**
 * 验证路由权限
 * @param to 将要请求的路由
 */
const isPermThisRoute = (to) => {
    // 获取路由状态仓库
    let routesStore = useRoutesStore();
    let routes = routesStore.routes;
    return routes.find((route) => route.path === to.path)
}

export default router


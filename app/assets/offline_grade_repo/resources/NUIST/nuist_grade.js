// 南京信息工程大学成绩导入脚本（独立成绩脚本仓库）

function getErrorMessage(error) {
    if (error && typeof error.message === "string" && error.message.trim()) return error.message;
    if (typeof error === "string" && error.trim()) return error;
    return "未知错误";
}

async function postJson(url, body) {
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "X-Requested-With": "XMLHttpRequest"
        },
        body,
        credentials: "include"
    });
    if (!response.ok) throw new Error(`接口请求失败（HTTP ${response.status}）`);
    return response.json();
}

function displayValue(row, key) {
    const display = row[`${key}_DISPLAY`];
    if (display !== null && display !== undefined && String(display).trim()) return String(display).trim();
    const raw = row[key];
    return raw === null || raw === undefined ? "" : String(raw).trim();
}

function toUnifiedGrade(row, index, semester) {
    const courseName = displayValue(row, "XSKCM") || displayValue(row, "KCM");
    if (!courseName) return null;
    const status = [displayValue(row, "CXCKDM"), displayValue(row, "XDFSDM") !== "正常" ? displayValue(row, "XDFSDM") : ""]
        .filter(Boolean).join(" / ");
    const composition = [["平时", row.PSCJXS], ["期中", row.QZCJXS], ["期末", row.QMCJXS]]
        .filter(item => item[1] !== null && item[1] !== undefined && String(item[1]).trim())
        .map(item => `${item[0]} ${item[1]}%`).join(" + ");
    return {
        id: displayValue(row, "WID") || displayValue(row, "XSKCH") || `${semester}-${index}`,
        courseName,
        courseType: displayValue(row, "KCXZDM") || displayValue(row, "KCLBDM") || "未分类",
        credits: displayValue(row, "XF") || "--",
        score: displayValue(row, "ZCJ") || displayValue(row, "XSZCJMC") || "暂无",
        gradePoint: displayValue(row, "XFJD") || "--",
        semester: displayValue(row, "XNXQDM") || semester,
        status: status || null,
        teacher: displayValue(row, "SKJS") || displayValue(row, "JSXM") || null,
        examType: displayValue(row, "KSLXDM") || null,
        scoreComposition: composition || null
    };
}

async function run() {
    try {
        const confirmed = await window.shiguangBridgePromise.showAlert(
            "南京信息工程大学成绩导入", "请先在当前教务页面完成登录。", "开始导入"
        );
        if (!confirmed) return;
        // 不传学期条件，一次读取当前账号的全部成绩，页面再按 semester 切换。
        const body = "pageSize=1000&pageNumber=1&*order=-XNXQDM,-KCH";
        const payload = await postJson("/jwapp/sys/cjcx/modules/cjcx/xscjcx.do", body);
        const table = payload?.datas?.xscjcx;
        if (!table) throw new Error("成绩接口返回数据格式异常。");
        const grades = (Array.isArray(table.rows) ? table.rows : []).map((row, index) => toUnifiedGrade(row, index, "")).filter(Boolean);
        if (grades.length === 0) throw new Error("未找到可导入的成绩记录。");
        await window.shiguangBridgePromise.saveImportedGrades(JSON.stringify({ records: grades }));
        window.shiguangBridge.showToast(`已解析全部学期 ${grades.length} 条成绩记录。`);
        window.shiguangBridge.notifyTaskCompletion();
    } catch (error) {
        window.shiguangBridge.showToast(`成绩导入失败：${getErrorMessage(error)}`);
        console.error("NUIST grade adapter error", error);
    }
}

run();

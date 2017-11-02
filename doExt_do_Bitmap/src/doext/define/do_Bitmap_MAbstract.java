package doext.define;

import org.json.JSONObject;

import core.DoServiceContainer;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoMultitonModule;

public abstract class do_Bitmap_MAbstract extends DoMultitonModule implements do_Bitmap_IMethod {

	protected do_Bitmap_MAbstract() throws Exception {
		super();
	}

	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception {
		super.onInit();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("getExif".equals(_methodName)) {
			this.getExif(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("loadFile".equals(_methodName)) {
			this.loadFile(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if (!checkProperty()) {
			DoServiceContainer.getLogEngine().writeError("do_Bitmap", new Exception("Bitmap 加载失败！"));
			return false;
		}
		if ("save".equals(_methodName)) {
			this.save(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("toGrayScale".equals(_methodName)) {
			this.toGrayScale(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("toFrostedGlass".equals(_methodName)) {
			this.toFrostedGlass(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("toRoundCorner".equals(_methodName)) {
			this.toRoundCorner(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("addWatermark".equals(_methodName)) {
			this.addWatermark(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	public abstract boolean checkProperty();
}
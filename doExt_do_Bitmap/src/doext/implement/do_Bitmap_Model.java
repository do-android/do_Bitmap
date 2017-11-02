package doext.implement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoImageLoadHelper;
import core.helper.DoImageLoadHelper.OnPostExecuteListener;
import core.helper.DoJsonHelper;
import core.helper.DoScriptEngineHelper;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIBitmap;
import core.interfaces.DoIPage;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoISourceFS;
import core.object.DoInvokeResult;
import core.object.DoMultitonModule;
import core.object.DoUIModule;
import doext.define.do_Bitmap_MAbstract;

/**
 * 自定义扩展MM组件Model实现，继承do_Bitmap_MAbstract抽象类，并实现do_Bitmap_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_Bitmap_Model extends do_Bitmap_MAbstract implements DoIBitmap {

	private Bitmap mBitmap;

	public do_Bitmap_Model() throws Exception {
		super();
	}

	/**
	 * 加载位图；
	 * 
	 * @throws Exception
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void loadFile(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {

		final String _source = DoJsonHelper.getString(_dictParas, "source", "");
		if (TextUtils.isEmpty(_source)) {
			throw new Exception("source 参数为空或者非法！");
		}

		if (null != DoIOHelper.getHttpUrlPath(_source)) {
			DoImageLoadHelper.getInstance().loadURL(_source, "never", -1, -1, new OnPostExecuteListener() {
				@Override
				public void onResultExecute(Bitmap bitmap, String url) {
					// url.equals(source)判断source等于最后请求结果URL并显示，忽略掉中间线程结果；
					if ((bitmap != null && url.equals(_source))) {
						mBitmap = bitmap;
						boolean _result = (mBitmap != null && !mBitmap.isRecycled());

						DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
						_invokeResult.setResultBoolean(_result);
						_scriptEngine.callback(_callbackFuncName, _invokeResult);
					}
				}
			});
		} else {
			if (_source != null && !"".equals(_source)) {
				String _path = DoIOHelper.getLocalFileFullPath(this.getCurrentPage().getCurrentApp(), _source);
				this.mBitmap = DoImageLoadHelper.getInstance().loadLocal(_path, -1, -1);
				boolean _result = (this.mBitmap != null && !this.mBitmap.isRecycled());

				DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
				_invokeResult.setResultBoolean(_result);
				_scriptEngine.callback(_callbackFuncName, _invokeResult);
			}
		}

	}

	/**
	 * 保存位图；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void save(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		String _format = DoJsonHelper.getString(_dictParas, "format", "JPEG");
		int _quality = DoJsonHelper.getInt(_dictParas, "quality", 100);
		String _outPath = DoJsonHelper.getString(_dictParas, "outPath", "");
		boolean _isUseDefault = false;
		if (_quality < 0 || _quality > 100) {
			_quality = 100;
		}
		CompressFormat _cFormat = CompressFormat.JPEG;
		String _fileName = DoTextHelper.getTimestampStr() + ".jpg.do";
		if ("PNG".equalsIgnoreCase(_format)) {
			_cFormat = CompressFormat.PNG;
			_fileName = DoTextHelper.getTimestampStr() + ".png.do";
		}
		String _fillPath = "";
		try {
			_fillPath = DoIOHelper.getLocalFileFullPath(this.getCurrentPage().getCurrentApp(), _outPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (TextUtils.isEmpty(_fillPath)) {
			_isUseDefault = true;
			_fillPath = _scriptEngine.getCurrentApp().getDataFS().getRootPath() + "/temp/do_Bitmap/" + _fileName;
		}

		File _outFile = new File(_fillPath);
		if (!DoIOHelper.existFile(_fillPath)) {
			DoIOHelper.createFile(_fillPath);
		}
		OutputStream _outputStream = new FileOutputStream(_outFile);
		boolean _result = this.mBitmap.compress(_cFormat, _quality, _outputStream);
		_outputStream.close();

		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());

		String _resultText = "";
		if (_result) {
			if (_isUseDefault) {
				_resultText = "data://temp/do_Bitmap/" + _fileName;
			} else {
				_resultText = _outPath;
			}
		}
		_invokeResult.setResultText(_resultText);
		_scriptEngine.callback(_callbackFuncName, _invokeResult);
	}

	/**
	 * 转成毛玻璃位图；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void toFrostedGlass(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		int _degree = DoJsonHelper.getInt(_dictParas, "degree", -1);
		if (_degree < 0 || _degree > 100) {
			throw new Exception("degree 参数为空或者不合法！");
		}

		boolean _result = false;
		Bitmap bmpTarget = DoFastBlur.blur(mBitmap, _degree);
		if (null != bmpTarget) {
			Bitmap newBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(newBitmap);
			canvas.scale(DoFastBlur.SCALEFACTOR, DoFastBlur.SCALEFACTOR);
			Paint paint = new Paint();
			paint.setFlags(Paint.FILTER_BITMAP_FLAG);
			canvas.drawBitmap(bmpTarget, 0, 0, paint);
			this.mBitmap = newBitmap;
			_result = true;
		}

		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		_invokeResult.setResultBoolean(_result);
		_scriptEngine.callback(_callbackFuncName, _invokeResult);

	}

	/**
	 * 转成灰色位图；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void toGrayScale(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {

		boolean _result = false;
		Bitmap bmpTarget = toGrayscale(mBitmap);
		if (null != bmpTarget) {
			this.mBitmap = bmpTarget;
			_result = true;
		}

		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		_invokeResult.setResultBoolean(_result);
		_scriptEngine.callback(_callbackFuncName, _invokeResult);
	}

	/**
	 * 添加圆角；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void toRoundCorner(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		int _radius = DoJsonHelper.getInt(_dictParas, "radius", 0);
//		double _zoom = (_scriptEngine.getCurrentPage().getRootView().getXZoom() + _scriptEngine.getCurrentPage().getRootView().getYZoom()) / 2;
//		_radius = (int) (_radius * _zoom);
		boolean _result = false;
		Bitmap bmpTarget = toRoundCorner(mBitmap, _radius);
		if (null != bmpTarget) {
			this.mBitmap = bmpTarget;
			_result = true;
		}

		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		_invokeResult.setResultBoolean(_result);
		_scriptEngine.callback(_callbackFuncName, _invokeResult);
	}

	/**
	 * 把图片变成圆角
	 * 
	 * @param bitmap
	 *            需要修改的图片
	 * @param pixels
	 *            圆角的弧度
	 * @return 圆角图片
	 */
	private Bitmap toRoundCorner(Bitmap bitmap, int roundPx) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * 图片去色,返回灰度图片
	 * 
	 * @param bmpOriginal
	 *            传入的图片
	 * @return 去色后的图片
	 */
	private Bitmap toGrayscale(Bitmap bmpOriginal) {
		Bitmap bmpGrayscale = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), bmpOriginal.getConfig());
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	@Override
	public boolean checkProperty() {
		return mBitmap != null && !mBitmap.isRecycled();
	}

	@Override
	public void setData(Bitmap _bitmap) {
		this.mBitmap = _bitmap;
	}

	@Override
	public Bitmap getData() {
		return this.mBitmap;
	}

	/**
	 * 获取图片拍摄信息；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void getExif(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _source = DoJsonHelper.getString(_dictParas, "source", "");
		if (TextUtils.isEmpty(_source))
			throw new Exception("path 不能为空");

		if (_source.startsWith(DoISourceFS.DATA_PREFIX) || _source.startsWith(DoISourceFS.SOURCE_PREFIX)) {

			String _filePath = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentPage().getCurrentApp(), _source);
			try {
				ExifInterface exifInterface = new ExifInterface(_filePath);
				// width
				String _width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
				// height
				String _height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
				// make
				String _make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
				// model
				String _model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
				// ExposureTime
				String _exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
				// FNumber
				String _fNumber = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
				// ISO
				String _iSO = exifInterface.getAttribute(ExifInterface.TAG_ISO);
				// date
				String _date = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
				// FocalLength
				String _focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
				// Lightsource
				String _lightsource = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
				// Flash
				String _flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
				JSONObject _jsonObject = new JSONObject();
				_jsonObject.put("Width", _width);
				_jsonObject.put("Height", _height);
				_jsonObject.put("Make", _make);
				_jsonObject.put("Model", _model);
				_jsonObject.put("ExposureTime", _exposureTime);
				_jsonObject.put("FNumber", _fNumber);
				_jsonObject.put("ISO", _iSO);
				_jsonObject.put("Date", _date);
				_jsonObject.put("FocalLength", _focalLength);
				_jsonObject.put("Lightsource", _lightsource);
				_jsonObject.put("Flash", _flash);
				_invokeResult.setResultNode(_jsonObject);
			} catch (Exception e) {
				DoServiceContainer.getLogEngine().writeError("do_Bitmap", e);
			}
		} else {
			throw new Exception("source参数只支持" + DoISourceFS.DATA_PREFIX + "或者" + DoISourceFS.SOURCE_PREFIX + " 打头!");
		}
	}

	@Override
	public void addWatermark(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
//		["type","source","percentX","percentY","fontColor","fontStyle","textFlag","fontSize"]
		String _type = DoJsonHelper.getString(_dictParas, "type", "");
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		if (TextUtils.isEmpty(_type)) {
			_invokeResult.setError("type 参数不能为空！");
			_invokeResult.setResultBoolean(false);
			throw new Exception("type 参数不能为空！");
		}

		String _source = DoJsonHelper.getString(_dictParas, "source", "");
		if (TextUtils.isEmpty(_source)) {
			_invokeResult.setError("source 参数不能为空！");
			_invokeResult.setResultBoolean(false);
			throw new Exception("source 参数不能为空！");
		}

		int _percentX = DoJsonHelper.getInt(_dictParas, "percentX", 50);
		int _percentY = DoJsonHelper.getInt(_dictParas, "percentY", 50);

		// 目前不支持，以后再支持
		// float _angle = DoJsonHelper.getInt(_dictParas, "angle", 0);

		boolean _result = false;
		if ("image".equalsIgnoreCase(_type)) {
			_result = createImageWatermark(_source, _percentX, _percentY, _scriptEngine, 0);
		} else if ("text".equalsIgnoreCase(_type)) {
			int _fontColor = DoUIModuleHelper.getColorFromString(DoJsonHelper.getString(_dictParas, "fontColor", "000000FF"), Color.BLACK);
			String _fontStyle = DoJsonHelper.getString(_dictParas, "fontStyle", "normal");
			String _textFlag = DoJsonHelper.getString(_dictParas, "textFlag", "normal");
			float _fontSize = getDeviceFontSize(_scriptEngine.getCurrentPage(), DoJsonHelper.getInt(_dictParas, "fontSize", 17) + "");

			boolean _isUnderline = false;
			boolean _isStrikethrough = false;

			if ("underline".equals(_textFlag)) { // 下划线
				_isUnderline = true;
			} else if ("strikethrough".equals(_textFlag)) { // 删除线
				_isStrikethrough = true;
			}

			Context c = DoServiceContainer.getPageViewFactory().getAppContext();
			Resources r;
			if (c == null) {
				r = Resources.getSystem();
			} else {
				r = c.getResources();
			}
			_fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, _fontSize, r.getDisplayMetrics());

			TextPaint _mPaint = new TextPaint();

			_mPaint.setColor(_fontColor);
//			_mPaint.setStrokeCap(Cap.ROUND);
			_mPaint.setStrokeWidth(getStrokeWidth(_scriptEngine.getCurrentPage(), 1));
			_mPaint.setStyle(Style.FILL);

			_mPaint.setStrikeThruText(_isStrikethrough); // 删除线
			_mPaint.setUnderlineText(_isUnderline); // 下划线

//			_mPaint.setTextAlign(mTextAlign);
			_mPaint.setTextSize(_fontSize);
//			mPaint.setTypeface(mTypeface);

			if ("bold".equals(_fontStyle)) { // 粗体
				setTypeface(Typeface.DEFAULT, Typeface.BOLD, _mPaint);
			} else if ("italic".equals(_fontStyle)) { // 斜体
				setTypeface(Typeface.MONOSPACE, Typeface.ITALIC, _mPaint);
			} else if ("bold_italic".equals(_fontStyle)) { // 粗斜体
				setTypeface(Typeface.MONOSPACE, Typeface.BOLD_ITALIC, _mPaint);
			} else { // normal
				setTypeface(Typeface.DEFAULT, Typeface.NORMAL, _mPaint);
			}
//			_mPaint.setStyle(mStyle);
			_result = createTextWatermark(_source, _percentX, _percentY, _scriptEngine, _mPaint, 0);

		}
		_invokeResult.setResultBoolean(_result);
		_scriptEngine.callback(_callbackFuncName, _invokeResult);

	}

	private boolean createTextWatermark(String _mText, int _percentX, int _percentY, DoIScriptEngine _scriptEngine, TextPaint _mPaint, float _angle) {
		if (mBitmap == null) {
			return false;
		}

		// 创建一个bitmap
		Bitmap newBmp = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		// 将该图片作为画布
		Canvas canvas = new Canvas(newBmp);
		// 在画布 0，0坐标上开始绘制原始图片
		canvas.drawBitmap(mBitmap, 0, 0, null);
		FontMetrics fm = _mPaint.getFontMetrics();
		String[] Strs = _mText.split("\n");
		float _textWidth = getRealWidth(Strs, _mPaint);
		float _textHeight = (float) (Math.ceil(fm.descent - fm.ascent) + 1) * Strs.length;
		float _left = (mBitmap.getWidth() * _percentX / 100) - (_textWidth / 2);
		float _top = (mBitmap.getHeight() * _percentY / 100) - (_textHeight / 2);
		StaticLayout staticLayout = new StaticLayout(_mText, _mPaint, canvas.getWidth(), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		canvas.translate(_left, _top);
		// 旋转角度
//		if (_angle != 0) {
//			canvas.rotate(-_angle);
//		}
		staticLayout.draw(canvas);
		canvas.save();
		canvas.restore();
		mBitmap = newBmp;
		return true;
	}

	private float getRealWidth(String[] text, TextPaint mPaint) {
		String maxStr = "";
		for (int index = 0; index < text.length; index++) {
			String itemStr = text[index];
			if (itemStr.length() > maxStr.length()) {
				maxStr = itemStr;
			}
		}
		return mPaint.measureText(maxStr);
	}

	private void setTypeface(Typeface tf, int style, Paint mPaint) {
		if (style > 0) {
			if (tf == null) {
				tf = Typeface.defaultFromStyle(style);
			} else {
				tf = Typeface.create(tf, style);
			}

			setTypeface(tf, mPaint);
			// now compute what (if any) algorithmic styling is needed
			int typefaceStyle = tf != null ? tf.getStyle() : 0;
			int need = style & ~typefaceStyle;
			mPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
			mPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
		} else {
			mPaint.setFakeBoldText(false);
			mPaint.setTextSkewX(0);
			setTypeface(tf, mPaint);
		}
	}

	private void setTypeface(Typeface tf, Paint mPaint) {
		if (mPaint.getTypeface() != tf) {
			mPaint.setTypeface(tf);
		}
	}

	private static int getStrokeWidth(DoIPage _page, int w) {
		if (_page == null) {
			return w + 5;
		}
		DoUIModule _uiModule = _page.getRootView();
		return DoUIModuleHelper.getCalcValue(w * (_uiModule.getXZoom() + _uiModule.getYZoom()) / 2);
	}

	private boolean createImageWatermark(String _source, float _percentX, float _percentY, DoIScriptEngine _scriptEngine, float _angle) throws Exception {
		Bitmap _watermarkBmp = null;
		if (_source.startsWith("@")) {
			DoMultitonModule _multitonModule = DoScriptEngineHelper.parseMultitonModule(_scriptEngine, _source);
			if (_multitonModule != null) {
				if (_multitonModule instanceof DoIBitmap) {
					_watermarkBmp = ((DoIBitmap) _multitonModule).getData();
				}
			}
		}

		if (_watermarkBmp == null) {
			String _path = DoIOHelper.getLocalFileFullPath(this.getCurrentPage().getCurrentApp(), _source);
			_watermarkBmp = DoImageLoadHelper.getInstance().loadLocal(_path, -1, -1);
		}

		if (mBitmap == null || _watermarkBmp == null) {
			return false;
		}

		// 创建一个bitmap
		Bitmap _newBmp = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		// 将该图片作为画布
		Canvas _canvas = new Canvas(_newBmp);
		// 在画布 0，0坐标上开始绘制原始图片
		_canvas.drawBitmap(mBitmap, 0, 0, null);

		float _left = (mBitmap.getWidth() * _percentX / 100) - (_watermarkBmp.getWidth() / 2);
		float _top = (mBitmap.getHeight() * _percentY / 100) - (_watermarkBmp.getHeight() / 2);
//		Matrix _matrix = new Matrix();
//		_matrix.setTranslate(mBitmap.getWidth() * _percentX / 100, mBitmap.getHeight() * _percentY / 100); //设置图片的旋转中心，即绕（X,Y）这点进行中心旋转
//		_matrix.preRotate(_angle, (float) _watermarkBmp.getWidth() / 2, (float) _watermarkBmp.getHeight() / 2); //要旋转的角度
		// 在画布上绘制水印图片
//		_canvas.drawBitmap(_watermarkBmp, _matrix, null);
		_canvas.drawBitmap(_watermarkBmp, _left, _top, null);
		// 旋转角度
//		if (_angle != 0) {
//			_canvas.rotate(-_angle, _left, _top);
//		}

		// 保存
		_canvas.save(Canvas.ALL_SAVE_FLAG);
		// 存储
		_canvas.restore();
//		mBitmap.recycle();
//		mBitmap = null;
		mBitmap = _newBmp;
		return true;
	}

	public static int getDeviceFontSize(DoIPage _page, String _fontSize) {
		int _convertFontSize = DoTextHelper.strToInt(_fontSize, 17);
		if (_page == null) {
			return _convertFontSize;
		}
		DoUIModule _uiModule = _page.getRootView();
		if (_uiModule == null) {
			return _convertFontSize;
		}
		int _convertSize = (int) Math.round(_convertFontSize * Math.min(_uiModule.getXZoom(), _uiModule.getYZoom()));
		if (_convertSize <= 0)
			_convertSize = 1;
		if (_convertSize > 32767)
			_convertSize = 32767;
		return _convertSize;
	}

}

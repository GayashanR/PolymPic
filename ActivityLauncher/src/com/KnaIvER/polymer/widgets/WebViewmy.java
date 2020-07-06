package com.KnaIvER.polymer.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;

import androidx.annotation.RequiresApi;

import com.KnaIvER.polymer.BrowserActivity;
import com.KnaIvER.polymer.R;
import com.KnaIvER.polymer.Utils.CMN;
import com.KnaIvER.polymer.Utils.Options;
import com.KnaIvER.polymer.toolkits.Utils.BU;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebViewmy extends WebView {
	public static float minW;
	public BrowserActivity.TabHolder holder;
	public Context context;
	public int currentPos;
	public int frameAt;
	public String toTag;
	public long time;
	public boolean fromCombined;
	public int lastScroll;
	int ContentHeight=0;
	public int HistoryVagranter=-1;
	public int expectedPos=-1;
	public int expectedPosX=-1;
	//public ArrayList<myCpr<String, ScrollerRecord>> History = new ArrayList<>();
	public float lastY;
	public File stackpath;
	public boolean loaded;
	
	/*public ArrayList<String> backstack;
	public int postRestoreI;
	public boolean restoringstack;
	class PostRestoreBackstack implements Runnable {
		@Override
		public void run() {
			CMN.Log("PostRestoreBackstack run...", postRestoreI, copyBackForwardList().getSize());
			int i=postRestoreI;
			int size = backstack.size();
			if(i<size) {
				loadUrl(backstack.get(i));
				postRestoreI=i+1;
				if(i>=size-1) {
					restoringstack=false;
					CMN.Log("restoringstack!", copyBackForwardList().getSize());
				} else {
					postDelayed(new PostRestoreBackstack(), 35);
				}
			}
		}
	}*/
	
	public WebViewmy(Context context) {
		this(context, null);
	}

	public WebViewmy(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WebViewmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context=context;
		init();
	}


	private void init(){
		//setBackgroundColor(Color.parseColor("#C7EDCC"));
		//setBackgroundColor(0);
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		//setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

		//网页设置初始化
		final WebSettings settings = getSettings();

		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		//	settings.setSafeBrowsingEnabled(false);
		//}

		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setDefaultTextEncodingName("UTF-8");

		settings.setNeedInitialFocus(false);
		//settings.setDefaultFontSize(40);
		//settings.setTextZoom(100);
		//setInitialScale(25);

		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);
		settings.setMediaPlaybackRequiresUserGesture(false);

		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);

		settings.setAllowFileAccess(true);

		//settings.setUseWideViewPort(true);//设定支持viewport
		//settings.setLoadWithOverviewMode(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		//settings.setSupportZoom(support);

		settings.setAllowUniversalAccessFromFileURLs(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}

		//settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);  //设置 缓存模式

		p2 = new Paint();
		p3 = new Paint();
		p2.setColor(Color.parseColor("#ffffff"));
		p2.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
		p3.setColor(Color.YELLOW);
		p3.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
		//setLayerType(View.LAYER_TYPE_HARDWARE, null);
		webScale=getResources().getDisplayMetrics().density;

	}
	public int getContentHeight(){
		//if(ContentHeight==0)
		ContentHeight = computeVerticalScrollRange();
		return ContentHeight;
	}
	public int getContentOffset(){
		return this.computeVerticalScrollOffset();
	}

	Paint p2,p3;

	//onDraw表示显示完毕
	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.drawRect(0,0,200,200, p3);
		//canvas.drawPath(path, paint);
		super.onDraw(canvas);
		// canvas.drawRect(0,0,200,200, p2);
	}

	@Override
	public void scrollTo(int x, int y) {
		//CMN.Log("webscrollTo "+x+" TO "+y);
		super.scrollTo(x, y);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		//CMN.Log(lastScroll, " onScrollChanged", l, t, oldl, oldt); //有的网页监听不到
		if(!Options.getAlwaysRefreshThumbnail() && Math.abs(lastScroll-t)>100){
			time=System.currentTimeMillis();
			lastScroll=t;
		}
		if(SrollChanged!=null)
			SrollChanged.onScrollChange(this,l,t,oldl,oldt);
	}
	public void setOnSrollChangedListener(ListViewmy.OnScrollChangeListener onSrollChangedListener) {
		SrollChanged=onSrollChangedListener;
	}
	ListViewmy.OnScrollChangeListener SrollChanged;

	public boolean isloading=false;
	@Override
	public void loadDataWithBaseURL(String baseUrl,String data,String mimeType,String encoding,String historyUrl) {
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		//if(!baseUrl.equals("about:blank"))
		isloading=true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		//CMN.Log("onSizeChanged  ");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//CMN.Log("onMeasure  ");
	}

	@Override
	public void loadUrl(String url) {
		super.loadUrl(url);
		loaded = true;
		CMN.Log("loadUrl: "+url.equals("about:blank"));
		//if(!url.equals("about:blank"))
		isloading=true;
	}

	@Override
	public void setWebViewClient(WebViewClient client){
		super.setWebViewClient(wvclient=client);
	}
	public WebViewClient wvclient;
	protected boolean MyMenuinversed;

	@Override
	protected void onCreateContextMenu(ContextMenu menu){
		//Toast.makeText(getContext(), "ONCCM", 0).show();
		super.onCreateContextMenu(menu);
	}

	public boolean bIsActionMenuShown;
	public callbackme callmeback;


	public void onFinishedPage() {
		if(wvclient!=null)
			wvclient.onPageFinished(this, "file:///");
	}
	
	
	public Bitmap getBitmap() {
		long id = Thread.currentThread().getId();
		CMN.Log("getting bitmap from webview...", id);
		
		int w = getWidth();
		
		int h = getHeight();
		
		if(w>0) {
			float factor = 1;
			if(w>minW) {
				factor = minW/w;
			}
			//CMN.Log("getting scale factor", factor);
			int targetW = (int)(w*factor);
			int targetH = (int)(h*factor);
			Bitmap b = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.RGB_565);
			Canvas c = new Canvas(b);
			c.scale(factor, factor);
			c.translate(0, -getScrollY());
			draw(c);
			return b;
		}
		return null;
	}

	public boolean loadIfNeeded() {
		if(holder.url!=null && !holder.url.equals(getTag())){
			if(stackpath==null) {
				stackpath = new File(getContext().getExternalCacheDir(), "webstack"+holder.id);
				CMN.Log("stackpath", stackpath);
				if(stackpath.exists()) {
					Parcel parcel = Parcel.obtain();
					byte[] data = BU.fileToByteArr(stackpath);
					parcel.unmarshall(data, 0, data.length);
					parcel.setDataPosition(0);
					Bundle bundle = new Bundle();
					bundle.readFromParcel(parcel);
					parcel.recycle();
					WebBackForwardList stacks = restoreState(bundle);
					if(stacks!=null && stacks.getSize()>0) {
						CMN.Log("复活……", stacks.getSize());
						return true;
					}
				}
			}
			CMN.Log("再生……", holder.url);
			loadUrl(holder.url);
			return true;
		}
		return false;
	}
	
	public void saveIfNeeded() {
		if((true||loaded) && stackpath!=null) {
			Bundle bundle = new Bundle();
			WebBackForwardList stacks = saveState(bundle);
			if(stacks!=null && stacks.getSize()>0) {
				Parcel parcel = Parcel.obtain();
				parcel.setDataPosition(0);
				bundle.writeToParcel(parcel, 0);
				byte[] bytes = parcel.marshall();
				parcel.recycle();
				BU.printFile(bytes, stackpath.getPath());
				CMN.Log("飞升……");
			} else {
				stackpath.delete();
			}
		}
	}
	
	@Override
	public void goBack() {
		super.goBack();
		loaded=true;
	}
	
	@Override
	public void goForward() {
		super.goForward();
		loaded=true;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	private class callbackme extends ActionMode.Callback2{
		ActionMode.Callback callback;
		public callbackme callhere(ActionMode.Callback callher) {callback=callher;return this;}
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			return bIsActionMenuShown=callback.onCreateActionMode(mode, menu);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return callback.onPrepareActionMode(mode, menu);
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()) {
				case R.id.toolbar_action0:
					evaluateJavascript(getHighLightIncantation(),new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							CMN.Log(value);
							
							invalidate();
						}});

					MyMenuinversed=!MyMenuinversed;
					return true;
				case R.id.toolbar_action1://工具复用，我真厉害啊啊啊啊！
					//evaluateJavascript("document.execCommand('selectAll'); console.log('dsadsa')",null);
					View cover=((ViewGroup) getParent()).getChildAt(0).findViewById(R.id.cover);
					cover.setTag(0);
					cover.performClick();
					return false;
			}
			return callback.onActionItemClicked(mode, item);
		}
		PopupWindow mPopup;
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			bIsActionMenuShown=false;
			//Toast.makeText(getContext(),"onDestroyActionMode", 0).show();
		}

		@Override
		public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
			if(ActionMode.Callback2.class.isInstance(callback))
				((ActionMode.Callback2)callback).onGetContentRect(mode, view, outRect);
			else
				super.onGetContentRect(mode, view, outRect);
			//Toast.makeText(getContext(),"onGetContentRect"+(view==WebViewmy.this), 0).show();
		}
	}
	//Viva Marshmallow!
	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		MyMenuinversed=false;
		if(callmeback==null) callmeback =new callbackme();
		ActionMode mode = super.startActionMode(callmeback.callhere(callback),type);
		//if(true) return mode;
		//Toast.makeText(getContext(), mode.getTag()+"ONSACTM"+mode.hashCode(), 0).show();
		//if(true) return mode;
		//mode.setTag(110);
		final Menu menu = mode.getMenu();
		int gid=0;
		if(menu.size()>0) {
			MenuItem item0 = menu.getItem(0);
			if(item0.getTitle().toString().startsWith("地") || item0.getTitle().toString().startsWith("Map"))
				menu.removeItem(item0.getItemId());
			if(menu.size()>0) gid=menu.getItem(0).getGroupId();
		}

		int highlightColor=Color.YELLOW;
		String ColorCurse = String.format("%06X", highlightColor&0xFFFFFF);
		Spanned text = Html.fromHtml("<span style='background:#"+ColorCurse+"; color:#"+ColorCurse+";'>高亮</span>");

		MenuItem MyMenu = menu.add(gid, R.id.toolbar_action0, 0, text);

		//Toast.makeText(getContext(),""+MyMenu.view,0).show();
		MyMenu = null;
		//MyMenu.get

		//Toast.makeText(getContext(),"asd"+menu.findItem(android.R.id.),0).show();
		//Toast.makeText(getContext(), MyMenu.getIntent()+""+MyMenu.getTitle()+" "+MyMenu.getItemId()+getResources().getString(android.R.string.share),0).show();
		//Toast.makeText(getContext(), ""+getResources().getString(getReflactField("com.android.internal.R$string", "share")),0).show();
		//Toast.makeText(getContext(),menu.getItem(3).getItemId()+"="+menu_share_id+"finding menu_share:"+menu.findItem(menu_share_id)+"="+android.R.id.shareText,0).show();

		String shareText="分享";
		int ShareString_Id=Resources.getSystem().getIdentifier("share","string", "android");
		if(ShareString_Id!=0)
			shareText=getResources().getString(ShareString_Id);
		String SelectAllText=getResources().getString(android.R.string.selectAll);
		int findCount=2;
		int ToolsOrder=0;
		for(int i=0;i<menu.size();i++) {
			String title = menu.getItem(i).getTitle().toString();
			if(title.equals(shareText)) {
				menu.removeItem(menu.getItem(i).getItemId());//移除 分享
				i--;
				findCount--;
			}else if(title.equals(SelectAllText)) {
				ToolsOrder=menu.getItem(i).getOrder();
				menu.removeItem(menu.getItem(i).getItemId());//移除 全选
				i--;
				findCount--;
			}
			if(findCount==0) break;
		}

		MyMenu=menu.add(gid,R.id.toolbar_action1,ToolsOrder+1,R.string.tools);



		//Toast.makeText(menu.getItem(0).getTitle()).show();
		return mode;
	}


   /**
	//Rangy serialize/deserialize functions
	var UNDEF = "undefined";
	function getNodeIndex(node) {
        var i = 0;
        while( (node = node.previousSibling) ) {
            ++i;
        }
        return i;
    }

	function serializePosition(node, offset, rootNode) {
		var pathParts = [], n = node;
		while (n && n != rootNode) {
			pathParts.push(getNodeIndex(n, true));
			n = n.parentNode;
		}
		return pathParts.join("/") + ":" + offset;
	}

    function getDocumentRangy(node) {
        if (node.nodeType == 9) {
            return node;
        } else if (typeof node.ownerDocument != UNDEF) {
            return node.ownerDocument;
        } else if (typeof node.document != UNDEF) {
            return node.document;
        } else if (node.parentNode) {
            return getDocumentRangy(node.parentNode);
        } else {
            throw module.createError("getDocument: no document found for node");
        }
    }

	function serializeRange(range) {
		var rootNode = getDocumentRangy(range.startContainer).documentElement;
		var serialized = serializePosition(range.startContainer, range.startOffset, rootNode) + "," +
			serializePosition(range.endContainer, range.endOffset, rootNode);
		return serialized;
	}

	function RangyDomPosition(node, offset) {
        this.node = node;
        this.offset = offset;
    }

	function deserializePosition(serialized, rootNode, doc) {
        var parts = serialized.split(":");
        var node = rootNode;
        var nodeIndices = parts[0] ? parts[0].split("/") : [], i = nodeIndices.length, nodeIndex;

        while (i--) {
            nodeIndex = parseInt(nodeIndices[i], 10);
            if (nodeIndex < node.childNodes.length) {
                node = node.childNodes[nodeIndex];
            } else {
				console.log( " has no child with index " + nodeIndex + ", " + i);
				return;
            }
        }

        return new RangyDomPosition(node, parseInt(parts[1], 10));
    }

	function deserializeRange(serialized) {
		var doc = document;
		var rootNode = doc.documentElement;
		const deserializeRegex = /^([^,]+),([^,\{]+)(\{([^}]+)\})?$/;
        var result = deserializeRegex.exec(serialized);
        //todo checksum
        var start = deserializePosition(result[1], rootNode, doc), end = deserializePosition(result[2], rootNode, doc);
        var range = new Range();
		range.setStart(start.node, start.offset);
		range.setEnd(end.node, end.offset);
        return range;
    }
	
	//highlight functionality
	function getNextNode(b) {
		var a = b.firstChild;
		if (a) {
			return a
		}
		while (b) {
			if ((a = b.nextSibling)) {
				return a
			}
			b = b.parentNode
		}
	}
	function getNodesInRange(c) {
		var b = [];
		var f = c.startContainer;
		var a = c.endContainer;
		var d = c.commonAncestorContainer;
		var e;
		for (e = f.parentNode; e; e = e.parentNode) {
			b.push(e);
			if (e == d) {
				break
			}
		}
		b.reverse();
		for (e = f; e; e = getNextNode(e)) {
			b.push(e);
			if (e == a) {
				break
			}
		}
		return b
	}
	function getNodeIndex(b) {
		var a = 0;
		while ((b = b.previousSibling)) {
			++a
		}
		return a
	}
	function insertAfter(d, b) {
		var a = b.nextSibling,
			c = b.parentNode;
		if (a) {
				c.insertBefore(d, a)
			} else {
				c.appendChild(d)
			}
		return d
	}
	function splitDataNode(c, a) {
		var b = c.cloneNode(false);
		b.deleteData(0, a);
		c.deleteData(a, c.length - a);
		insertAfter(b, c);
		return b
	}
	function isCharacterDataNode(b) {
		var a = b.nodeType;
		return a == 3 || a == 4 || a == 8
	}
	function splitRangeBoundaries(b) {
		var f = b.startContainer,
			e = b.startOffset,
			c = b.endContainer,
			a = b.endOffset;
		var d = (f === c);
		if (isCharacterDataNode(c) && a > 0 && a < c.length) {
				splitDataNode(c, a)
			}
		if (isCharacterDataNode(f) && e > 0 && e < f.length) {
				f = splitDataNode(f, e);
				if (d) {
					a -= e;
					c = f
				} else {
					if (c == f.parentNode && a >= getNodeIndex(f)) {
						++a
					}
				}
				e = 0
			}
		b.setStart(f, e);
		b.setEnd(c, a)
	}
	function getTextNodesInRange(b) {
		var f = [];
		var a = getNodesInRange(b);
		for (var c = 0, e, d; e = a[c++];) {
			if (e.nodeType == 3) {
				f.push(e);
			}
		}
		return f;
	}
	function surroundRangeContents(b, g) {
		splitRangeBoundaries(b);
		var f = getTextNodesInRange(b);
		if (f.length == 0) {
			return;
		}
		for (var c = 0, e, d; e = f[c++];) {
			if (e.nodeType == 3) {
				d = g.cloneNode(false);
				e.parentNode.insertBefore(d, e);
				d.appendChild(e);
			}
		}
		b.setStart(f[0], 0);
		var a = f[f.length - 1];
		b.setEnd(a, a.length);
	}
 
	function HighlightSelection() {
		console.log('HighlightSelection called');
		if (window.getSelection) {
			 var spanner = document.createElement("span");
			 spanner.className = "PLOD_HL";
			 spanner.style = "background:#ffaaaa;";
			 var sel = window.getSelection();
			 if(sel.rangeCount) {
				var range = sel.getRangeAt(0);
				window._docAnnots+=serializeRange(range)+('|0'+'\n');
				surroundRangeContents(range, spanner)
			 }
			 //sel.removeAllRanges();
		}
		return window._docAnnots;
	}
	
	function recurseDeWrap(b) {
		if (b) {
			for (var e = b.length - 1, d; e >= 0; e--) {
				d = b[e];
				if (d.className == "PLOD_HL") {
					var c = 0;
					for (var f = d.childNodes.length - 1; f >= 0; f--) {
						var a = d.childNodes[f];
						if (!c) {
							c = d
						}
						d.parentNode.insertBefore(a, c);
						c = a
					}
					d.parentNode.removeChild(d)
				}
			}
		}
	}
	
	function DeHighlightSelection() {
		if (window.getSelection) {
			var spanner = document.createElement("span");
			spanner.className = "highlight";
			var sel = window.getSelection();
			var ranges = [];
			var range;
			for (var i = 0, len = sel.rangeCount; i < len; ++i) {
				ranges.push(sel.getRangeAt(i))
			} //sel.removeAllRanges();
			i = ranges.length;
			while (i--) {
				range = ranges[i];
				var nodes = getNodesInRange(range);
				recurseDeWrap(nodes)
			}
		};
	}
	*/
   @Multiline(trim=true)
	public final static byte[] commonIcanBytes = ArrayUtils.EMPTY_BYTE_ARRAY;
 
	/**
	 var script=undefined;
	 if(!window.PPOLKit) {
	 	 window._docAnnots = '';
		 script = document.createElement('script');
		 script.type = 'text/javascript';
		 script.src = 'mdbr://HIGHLIGHTS.js';
		 window.PPOLKit=1;
	 }
	 */
   @Multiline(trim=true)
	private final static String commonIcan = StringUtils.EMPTY;
	private static int commonIcanBaseLen;
 
 
	private static StringBuilder HighlightBuilder;

	/**
	 if(script) {
		 script.onload=function(){
	 		HighlightSelection();
			delete script.onload;
	 	};
	 	document.head.appendChild(script);
	 	'delay'
	 } else {
	 	HighlightSelection();
	 }
	 */
	@Multiline(trim=true)
	private final static String HighLightIncantation="HI";
	
	/**
	 if(script) {
		 script.onload=function(){
	 		DeHighlightSelection();
			//delete script.onload;
	 	};
	 	document.head.appendChild(script);
	 } else {
	 	DeHighlightSelection();
	 }
	 */
	@Multiline(trim=true)
	private final static  String DeHighLightIncantation="DEHI";
	
	public float webScale=0;
	public String getHighLightIncantation() {
		if(commonIcanBaseLen==0){
			HighlightBuilder = new StringBuilder(commonIcan);
			commonIcanBaseLen=HighlightBuilder.length();
		}
		HighlightBuilder.setLength(commonIcanBaseLen);
		return HighlightBuilder.append(HighLightIncantation).toString();
	}
	public String getDeHighLightIncantation() {
		if(commonIcanBaseLen==0){
			HighlightBuilder = new StringBuilder(commonIcan);
			commonIcanBaseLen=HighlightBuilder.length();
		}
		HighlightBuilder.setLength(commonIcanBaseLen);
		return HighlightBuilder.append(DeHighLightIncantation).toString();
	}

	public static int getReflactField(String className,String fieldName){
		int result = 0;
		try {
			Class<?> clz = Class.forName(className);
			Field field = clz.getField(fieldName);
			field.setAccessible(true);
			result = field.getInt(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static int[] getReflactIntArray(String className,String fieldName){
		int[] result = null;
		try {
			Class<?> clz = Class.forName(className);
			Field field = clz.getField(fieldName);
			field.setAccessible(true);
			result = (int[]) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	//@Override
	//public boolean zoomOut(){
	//	CMN.Log("zoomOut");
	//	return false;
	//}



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		lastY = event.getY();
		return super.onTouchEvent(event);
	}
}
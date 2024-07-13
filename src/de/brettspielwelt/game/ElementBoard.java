package de.brettspielwelt.game;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.Vect;
import de.brettspielwelt.develop.HTMLWrapper;
import de.brettspielwelt.game.ElementBoard.Place;

public abstract class ElementBoard extends HTMLWrapper{

	Vector history=new Vector();
	boolean rep=false;
	int animation=0;

	boolean noResponse=false;
	int animSleep=50, animLength=16, animDelta=animLength/4;
	int jpx,jpy,jpvx,jpvy,jpc;
	int[] jiy= {0,4,6,8,10,12,13,14,14,13,12,10,8,6,4,0,0,0,0};
	int[] jishrink= {255,240,220,200,180,160,140,120,100,80,60,40,40,40,40,40,40,40,40};
	//int[] jiy= {0,2,4,5,6,7,8,9,10,11,12,13,13,14,14,14,14,13,13,12,11,10,9,8,7,6,5,4,2,0,0,0,0,0};
	int[] animationArr;
	int[] flying,fsP1,fsP2,fsPT1,fsPT2;
	Place[] fp,fpt;

	class Place{
		int x, y;
		double scx=1.0,scy=1.0;
		int rot;
		int shear;
		int currentObject;
		public boolean drawLast;
		Place onPlace;
		int inScreen=-1;
		
		public Place(int x, int y) {
			this.x=x; this.y=y;
		}
		public Place(int x, int y, double sc) {
			this.x=x; this.y=y;
			scx=sc; scy=sc;
		}
		public Place(int x, int y, double sc, int rot) {
			this.x=x; this.y=y;
			scx=sc; scy=sc;
			this.rot=rot;
		}
		
		public AffineTransform getAffine() {
			if(inScreen>-1) {
				Rectangle ret=drawObj(null,inScreen,0);
				if(y+ret.y*scy<0) y=(int)(-ret.y*scy);
				if(y+(ret.y+ret.height)*scy>784) y=(int)(784+ret.y*scy);
				if(x+ret.x*scx<0) x=(int)(-ret.x*scx);
				if(x+(ret.x+ret.width)*scx>1220) x=(int)(1220+ret.x*scx);
			}
			AffineTransform af=new AffineTransform();
			af.translate(x, y);
			af.scale(scx, scy);
			af.rotate(rot*Math.PI/180);
			if(onPlace!=null) {
				AffineTransform nf=new AffineTransform(onPlace.getAffine());
				nf.concatenate(af);
				return nf;
			}
			return af;
		}
		
		public Place setOnPlace(Place p) {
			onPlace=p;
			return this;
		}
		public boolean getDrawLast() {
			if(onPlace!=null)
				return (onPlace.drawLast);
			return drawLast;
		}
		public Place setInScreen(int obj) {
			inScreen=obj;
			return this;
		}
		public Place setLater() {
			drawLast=true;
			return this;
		}
		
	}

	@Override
	public void init(int lev, Runnable run) {
		// TODO Auto-generated method stub
		
	}

	public void getBoard(Vect v) {
		
	}
	public abstract Place getPlace(int a, int b);
	public abstract Rectangle drawObj(Graphics2D g, int obj, int ti);
	int dummy=0;
	
	@Override
	public void run() {
//		if(dummy++<4) {
//			return;
//		}
//		dummy=0;
		if(history.size()==0) noResponse=false;
		if(animation==0 && history!=null && history.size()>0) {
			getBoard((Vect)history.elementAt(0));
			history.removeElementAt(0);
			rep=true;
			if(animation==1 && animationArr!=null && animationArr.length>0) {
				flying=new int[animationArr.length/5];
				fsP1=new int[animationArr.length/5];
				fsP2=new int[animationArr.length/5];
				fsPT1=new int[animationArr.length/5];
				fsPT2=new int[animationArr.length/5];
				fp=new Place[flying.length];
				fpt=new Place[flying.length];
				
				for(int i=0; i<flying.length; i++) {
					flying[i]=animationArr[i*5];
					fsP1[i]=animationArr[i*5+1];
					fsP2[i]=animationArr[i*5+2];
					fsPT1[i]=animationArr[i*5+3];
					fsPT2[i]=animationArr[i*5+4];
					fp[i]=getPlace(animationArr[i*5+1],animationArr[i*5+2]);
					fpt[i]=getPlace(animationArr[i*5+3],animationArr[i*5+4]);
				}
				jpc=0;
			}else { animation=0;  rep=false; }
		}
		if(animation==1) {
			if(jpc%animDelta==0 && jpc/animDelta<flying.length) {
				int targA=fsPT1[jpc/animDelta];
				int targB=fsPT2[jpc/animDelta];
				int obj=flying[jpc/animDelta];
				animationBegin(obj,fsP1[jpc/animDelta],fsP2[jpc/animDelta],targA,targB);
			}
			jpc++;
			if(jpc>=animLength){
				int w=(jpc-animLength);
				if(w%animDelta==0) {
					int targA=fsPT1[w/animDelta];
					int targB=fsPT2[w/animDelta];
					int obj=flying[w/animDelta];
					animationEnd(obj,fsP1[w/animDelta],fsP2[w/animDelta],targA,targB);
				}
			}
			if(jpc==animLength+(flying.length-1)*animDelta) {
				animation=0;
				if(history.size()==0) rep=true;
				//if(flying.length==1 && fsP1[0]==3) playSound(0);
			} 
		}
		
		if(rep || animation>0) {
			rep=false;
			repaint();
		}
	}

	void animationBegin(int obj, int a, int b, int targA, int targB) {
		System.err.println("animationBegin: "+obj+" - "+a+","+b+" --> "+targA+","+targB);
	}
	void animationEnd(int obj, int a, int b, int targA, int targB) {
		System.err.println("animationEnd: "+obj+" - "+a+","+b+" --> "+targA+","+targB);
	}

	@Override
	public void paintp(Graphics g) {
		// TODO Auto-generated method stub
		
	}

	public boolean hitLocObject(int x, int y, int a, int b, int obj) {
		Place p=getPlace(a,b);
		if(p==null) return false;
		AffineTransform tr=p.getAffine();
		Point2D dst=new Point2D.Double();
		try {
			tr.inverseTransform(new Point2D.Double(x, y), dst);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		Rectangle ret=drawObj(null, obj, 0);
		return ret.contains(dst); 
	}
	

	int[] jiyD= {12,14,16,18,14,10,7,4,0,3,4,2,0,2,3,2,0,0,0};
	
	List<Place> later=new ArrayList<Place>();
	
	public Rectangle drawLocObject(Graphics2D g, int posA, int posB, int obj){
		if(animation==1) {
			for(int i=0; i<flying.length; i++) {
				if(fsPT1[i]==posA && fsPT2[i]==posB) return null;
				if(jpc-i*4>=0){
					if(fsP1[i]==posA && fsP2[i]==posB) return null;		
				}
			}
		}
		Place p=getPlace(posA,posB);
		if(p.getDrawLast()) {
			p.currentObject=obj;
			later.add(p);
			return null;
		}
		save(g);
		g.setTransform(p.getAffine());
		Rectangle ret=drawObj(g, obj, 0);
		restore(g);
		return ret;
	}
	public void drawLater(Graphics2D g) {
		for(Place p: later) {
			save(g);
			g.setTransform(p.getAffine());
			drawObj(g, p.currentObject, 0);
			restore(g);
		}
		
		if(animation==1) {
			for(int i=0; i<flying.length; i++) {
				Place dst=fpt[i];
				Place src=fp[i];
				int rjpc=jpc-i*animDelta;
				if(rjpc>animLength) rjpc=animLength;
				if(rjpc>=0 && rjpc<=animLength) {
					save(g);
					g.setTransform(intermediate(src.getAffine(), dst.getAffine(), rjpc));
					drawObj(g,flying[i],rjpc);
					restore(g);
				}
			}
		}
	}
	
	public AffineTransform intermediate(AffineTransform src, AffineTransform dst, int i) {
        double[] srcMatrix = new double[6];
        double[] dstMatrix = new double[6];
        src.getMatrix(srcMatrix);
        dst.getMatrix(dstMatrix);
        double[] intermediateMatrix = new double[6];
        
        double t=ease(i,0,1,16);
        for (int j = 0; j < 6; j++) {
            intermediateMatrix[j] = srcMatrix[j] + t * (dstMatrix[j] - srcMatrix[j]);
        }
        
        return new AffineTransform(intermediateMatrix);
	}
	// t == aktuelle zeit / d = steps / rückgabe zwischen b und c
	//Standard-Version
	public double ease(double t, double b, double c, double d) {
		c-=b;
		double ts = (t /= d) * t;
		double tc = ts * t;
		return b + c * (tc + -3 * ts + 3 * t);
	}
	
	// KingdomBuilder-Karten
	public double ease2(double t, double b, double c, double d) {
		c-=b;
		double ts = (t /= d) * t;
		double tc = ts * t;
		return b + c * (4 * tc + -9 * ts + 6 * t);
	}
	
	// Hex fadeout bei Spielende
	public double ease3(double t, double b, double c, double d) {
		c-=b;
		double ts = (t /= d) * t;
		double tc = ts * t;
		return b + c * (tc * ts);
	}

	public void ghost(Graphics2D g, int level) {
		save(g);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,level/255.0f));
	}

	public void noGhost(Graphics2D g) {
		restore(g);
	}

	public long getMemoryUsage() {
		return 25L*1024L*1024L;
	}
}

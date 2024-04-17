import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class MainServer 
{
	static Connection con=null;
	static Statement st=null; 
	static ResultSet rs=null;

	@SuppressWarnings({ "unchecked", "rawtypes", "unused", "resource" })
    public static void main(String[] args) 
	{
		try
		{
			ServerSocket ssocket=new ServerSocket(8888);

			System.out.println("\n");
			System.out.println("\n");
			System.out.println("\n");
			System.out.println("\n");
			System.out.println("*******************Main Server ON*******************");
		
			while(true)
			{
				Socket socket=ssocket.accept();

				ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());

				String data=(String)ois.readObject();

				ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
				
				con=DatabaseConnection.getConnection();
				st=con.createStatement();

				//*******************************Register***********************************//
				
				if(data.equals("Register"))
				{
					String name=(String)ois.readObject();
					String portno=(String)ois.readObject();
					String ipadd=(String)ois.readObject();
					String status=(String)ois.readObject();

					String attack="OFF";
					String ataType="Null";
					String pubKey="Null";
					String macAdd="Null";
					String signa="Null";
					String npv="Null";

					rs=st.executeQuery("Select * from noderegister Where NodeName='"+name+"' ");

					if(rs.next())
					{
						oos.writeObject("AlreadyAvailable");
					}
					else
					{
						int i=st.executeUpdate("Insert into noderegister Values('"+name+"','"+portno+"','"+ipadd+"','"+status+"','"+attack+"','"+ataType+"','"+pubKey+"','"+macAdd+"','"+signa+"','"+npv+"')");

						oos.writeObject("Success");
					}
				}

				//***************************************Login*****************************************//

				else if(data.equals("Login"))
				{
					String name=(String)ois.readObject();
					String portno=(String)ois.readObject();

					rs=st.executeQuery("Select * from noderegister Where NodeName='"+name+"' and PortNo='"+portno+"' ");

					if(rs.next())
					{
						String ipadd=rs.getString(3);

						oos.writeObject("Success");

						oos.writeObject(name);
						oos.writeObject(portno);
						oos.writeObject(ipadd);

						st.executeUpdate("Update noderegister set Status='ON' where NodeName='"+name+"'");
					}
					else
					{
						oos.writeObject("Failed");
					}
				}

				//********************************DiscoverNeighborNodes************************************//

				else if (data.equals("Discover"))
				{
					Vector node=new Vector();

					String dname=(String)ois.readObject();

					rs=st.executeQuery("Select * From noderegister Where NodeName!='"+dname+"' and Status='ON'" );

					while(rs.next())
					{
						node.add(rs.getString(1));
					}
					oos.writeObject("DiscoverSuccess");
					oos.writeObject(node);
				}

				//********************************DiscoverNeighborNodes************************************//

				else if (data.equals("DiscoverRx"))
				{
					Vector node1=new Vector();

					String dname=(String)ois.readObject();

					rs=st.executeQuery("Select * From noderegister Where NodeName!='"+dname+"' and Status='ON'" );

					while(rs.next())
					{
						node1.add(rs.getString(1));
					}
					oos.writeObject("DiscoverRxSuccess");
					oos.writeObject(node1);
				}


				//***********************************Node Information(Send POLL)*********************************//

				else if(data.equals("NodeInfo"))
				{
					Vector nodelist=(Vector)ois.readObject();
					Vector nodedtl=new Vector();
					for(int i=0;i<nodelist.size();i++)
					{
						rs=st.executeQuery("Select PortNo, IpAddress from noderegister Where NodeName='"+nodelist.get(i).toString()+"' ");
					
						if(rs.next())
						{
							String por=rs.getString(1);
							String ip=rs.getString(2);

							int port=Integer.parseInt(por);
					
							nodedtl.add(port);
							nodedtl.add(ip);
						}	
					}
					oos.writeObject("NodeInf");
					oos.writeObject(nodedtl);
					System.out.println(nodedtl.toString());
				}
		//***********************************KeyGeneration************************************************//
				
				 else if(data.equals("Key"))
				{
					String na=(String)ois.readObject();
					String ky=(String)ois.readObject();

					st.executeUpdate("Update noderegister set PublicKey='"+ky+"' where NodeName='"+na+"'");
					
					oos.writeObject("keyUpdate");
				}

		//***********************************MAC Address Generation************************************************//

				  else if(data.equals("mac"))
				{
					String na=(String)ois.readObject();
					String mac=(String)ois.readObject();

					st.executeUpdate("Update noderegister set MacAddress='"+mac+"' where NodeName='"+na+"'");
					
					oos.writeObject("macUpdate");
				}

		//***********************************Signature Generation************************************************//
				 
				  else if(data.equals("sig"))
				{
					String na=(String)ois.readObject();
					String sig=(String)ois.readObject();

					st.executeUpdate("Update noderegister set Signature='"+sig+"' where NodeName='"+na+"'");
					
					oos.writeObject("SigUpdate");
				}

		//***********************************Node Information(Send REPLY)*********************************//
				
				else if(data.equals("NodeInfoRx"))
				{
					Vector nodelist=(Vector)ois.readObject();
					Vector nodedtl=new Vector();
					for(int i=0;i<nodelist.size();i++)
					{
						rs=st.executeQuery("Select PortNo, IpAddress, Attack from noderegister Where NodeName='"+nodelist.get(i).toString()+"' ");
					
						if(rs.next())
						{
							String name=nodelist.get(i).toString();
							String por=rs.getString(1);
							String ip=rs.getString(2);
							String attak=rs.getString(3);

							int port=Integer.parseInt(por);
							
							nodedtl.add(name);
							nodedtl.add(port);
							nodedtl.add(ip);
							nodedtl.add(attak);
						}	
					}
					oos.writeObject("NodeInfRx");
					oos.writeObject(nodedtl);
					System.out.println(nodedtl.toString());
				}
	
	//*************************************ATTACK Node Or Not****************************************************//

				else if(data.equals("NodeAttacked"))
				{
					String name=(String)ois.readObject();
					String attak="";

					rs=st.executeQuery("Select Attack from noderegister Where NodeName='"+name+"' ");

					if(rs.next())
					{
						attak=rs.getString(1);
						System.out.println(attak);
					}
					oos.writeObject("AttackInfo");
					oos.writeObject(attak);
				}

	//***********************************Report Node(Report Button)*********************************//
				
				else if(data.equals("ReportNode"))
				{
					String dname=(String)ois.readObject();

					String ip="";
					int port=0;
					rs=st.executeQuery("Select PortNo, IpAddress from noderegister Where NodeName='"+dname+"' ");
					
						if(rs.next())
						{
							String por=rs.getString(1);
							ip=rs.getString(2);

							port=Integer.parseInt(por);
						}
						oos.writeObject("RepNode");
						oos.writeObject(ip);
						oos.writeObject(port);
				}
	//*************************************ATTACK ON****************************************************//
				
				else if(data.equals("AttackON"))
				{
					String name=(String)ois.readObject();
					String atak=(String)ois.readObject();

					st.executeUpdate("Update noderegister set Attack='ON' where NodeName='"+name+"'");
					
					if(atak.isEmpty())
					{

					}
					else
					{
						st.executeUpdate("Update noderegister set AttackType='"+atak+"' where NodeName='"+name+"'");
					}

					oos.writeObject("Attacked");
				}

	//*************************************ATTACK REPORT****************************************************//

				else if(data.equals("ReportAttak"))
				{
					String on="ON";
					String attacknode="";
					rs=st.executeQuery("Select NodeName from noderegister Where Attack='"+on+"' ");

					if(rs.next())
					{
						attacknode=rs.getString(1);
					}
					System.out.println("attack nodes  ::::"+attacknode);

					oos.writeObject("ReportAtt");
					oos.writeObject(attacknode);
				}
//***************************************** Topology Construction*******************************************************//
				
				else if(data.equals("Topology"))
				{
					rs=st.executeQuery("Select NodeName From noderegister");
					
					Vector node=new Vector();

					while(rs.next())
					{
						node.add(rs.getString(1));
					}

					for(int i=0;i<node.size();i++)
					{
						for(int j=0;j<node.size();j++)
						{
							String source=node.get(i).toString();
							String desti=node.get(j).toString();

							if(source.equals(desti))
							{
							}
							else
							{
								st.executeUpdate("Insert into topologyconstruct values('"+source+"','"+desti+"')");	
								st.executeUpdate("Insert into npvnodes values('"+source+"','"+desti+"')");
									
								oos.writeObject("Success");
							}
						}
					}
				}


//********************************************ComboBox Node Select ***********************************************//

				else if(data.equals("AllNodes"))
				{
					Vector node=new Vector();

					String recname=(String)ois.readObject();

					rs=st.executeQuery("Select * From noderegister where NodeName!='"+recname+"' ");
					
					while(rs.next())
					{
						node.add(rs.getString(1));
					}
					oos.writeObject(node);
				}

//*********************************************checkAvailablePath******************************************************//
				
			else if(data.equals("PathInfo"))
			{
				Vector availPath=(Vector)ois.readObject();

				String sourceName=(String)availPath.get(0);
				String destiName=(String)availPath.get(1);
					
				PathInfo path=new PathInfo();
					
				Vector paths=path.getPath(sourceName,destiName);
					
				for(int i=0;i<paths.size();i++)
				{		
					int val=st.executeUpdate("insert into pathinfo values('"+paths.get(i).toString()+"')");
				}
				oos.writeObject("AvailPath");
				oos.writeObject(paths); 
			}

 
 //*********************************************NPV Success******************************************************//
				
			else if(data.equals("NPVVerified"))
			{
				String vnode=(String)ois.readObject();
				
				System.out.println("00000000000000000000000"+vnode);

				st.executeUpdate("Update noderegister SET npv='ON' where NodeName='"+vnode+"'");
			}

//***************************************************Send Option in Sender Side************************************************************//

			else if(data.equals("send"))
			{
				String filepath=(String)ois.readObject();
				String seldesti=(String)ois.readObject();
			//	String attak=(String)ois.readObject();

				String att="ON";
				String attackerNode="";
				String attType="";

				System.out.println(filepath+"\t"+seldesti);
				Vector vect=new Vector();

				String str[]=filepath.split("=>");
				for(int i=0;i<str.length;i++)
				{
					rs=st.executeQuery("Select * from noderegister Where NodeName='"+str[i]+"' and Status='ON' ");

					if(rs.next())
					{
						rs=st.executeQuery("Select * From noderegister Where NodeName='"+seldesti+"' and Status='ON' ");

						if(rs.next())
						{
							int port=rs.getInt(2);
							String ip=rs.getString(3);
							
							vect.add(port);
							vect.add(ip);
						}
					}
				}		

					rs=st.executeQuery("Select * from noderegister Where Attack='"+att+"' ");

					if(rs.next())
					{
						attackerNode=rs.getString(1);	
						attType=rs.getString(6);
					}

					System.out.println(vect.get(0).toString()+"\t"+vect.get(1).toString()+"\t"+attackerNode+"\t"+attType);
					oos.writeObject("Destination");
					oos.writeObject(vect);
					oos.writeObject(attackerNode);
					oos.writeObject(attType);
			}
//***************************************************Isolate************************************************************//

			else if(data.equals("Node Isolation"))
			{
				
				String sourceNode=(String)ois.readObject();
				String destiNodee=(String)ois.readObject();
				String isoatt=(String)ois.readObject();

				String att="ON";
				String npvOn="";

				rs=st.executeQuery("Select * from noderegister Where NodeName='"+sourceNode+"' ");
				if(rs.next())
				{
					npvOn=rs.getString(10);					
				}

				if(npvOn.equals("Null"))
				{
					oos.writeObject("NPVNotFound");
				}
				else
				{
					rs=st.executeQuery("Select NodeName from noderegister Where Attack='"+att+"' ");
					if(rs.next())
					{
						isoatt=rs.getString(1);					
					}

					System.out.println(sourceNode+"\t"+destiNodee+"\t"+isoatt);

					rs=st.executeQuery("select * from npvnodes where source='"+isoatt+"'");

					if(rs.next())
					{
						String desti=rs.getString(1);

						st.executeUpdate("delete from npvnodes where source='"+isoatt+"'");
					//st.executeUpdate("delete from isolatepath where Destination='"+desti+"'");
					}
					rs=st.executeQuery("select * from npvnodes where destination='"+isoatt+"'");
				
					if(rs.next())
					{
						String source=rs.getString(1);

						st.executeUpdate("delete from npvnodes where destination='"+isoatt+"'");
					//st.executeUpdate("delete from isolatepath where Source='"+source+"'");
					}

					IsolatePathInfo paths=new IsolatePathInfo();
					
					Vector isoPaths=paths.getPaths(sourceNode,destiNodee);	

					System.out.println("******************"+isoPaths);

					for(int j=0;j<isoPaths.size();j++)
					{	
						int val=st.executeUpdate("insert into isolatepathinfo values('"+isoPaths.get(j).toString()+"')");
					}

					String attackerNode="";
					Vector allPaths=new Vector();
				
			//	String count="1";
						
			//	rs=st.executeQuery("select * from noderegister where count='"+count+"' ");
							
			//	if(rs.next())
			//	{
			//		attackerNode=rs.getString(1);
							
					rs=st.executeQuery("select * from isolatepathinfo");
					while(rs.next())
					{	
						allPaths.add(rs.getString(1));
									
					}
					System.out.println(allPaths);
			//	}
				oos.writeObject("NPVProtocol");
		//		oos.writeObject(attackerNode);
				oos.writeObject(allPaths);
		//		oos.writeObject(isoPaths);
				}
			}


		}
	}
	catch (Exception a)
	{
		System.out.println(a);
	}
		
	}
}
/*
	2024 KHIT STUDENTS PROJECT
 */
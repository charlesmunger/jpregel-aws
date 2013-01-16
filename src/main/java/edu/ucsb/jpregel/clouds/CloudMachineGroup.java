/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsb.jpregel.clouds;

import api.MachineGroup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.io.Payloads;
import org.jclouds.ssh.SshClient;

/**
 *
 * @author Charles Munger
 */
public abstract class CloudMachineGroup<T> extends MachineGroup<T> {

	protected final ApiMetadata compute;
	protected final Set<? extends NodeMetadata> nodes;
	protected final ComputeService comp;
	protected final ComputeServiceContext context;
	public static final String JARNAME = "jpregel-aws.jar";
	private final File jar = new File("dist/" + JARNAME);


	@Inject
	CloudMachineGroup(Set<? extends NodeMetadata> nodes, ApiMetadata compute) {
		this.nodes = nodes;
		this.context = new ContextBuilder(compute).credentials(null, null).build(ComputeServiceContext.class);
		this.comp = context.getComputeService();
		this.compute = compute;
	}

	@Override
	public String getHostname() {
		return Iterables.getOnlyElement(
			Iterables.getOnlyElement(
			nodes).getPrivateAddresses());
	}

	@Override
	public void reset() throws IOException {
		try {
			comp.runScriptOnNodesMatching(new Predicate<NodeMetadata>() {

				public boolean apply(NodeMetadata t) {
					return nodes.contains(t);
				}
			}, "killall -9 java; cd; rm -rf *");
		} catch (RunScriptOnNodesException ex) {
			Logger.getLogger(CloudMachineGroup.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void terminate() throws IOException {
		comp.destroyNodesMatching(new Predicate<NodeMetadata>() {

			public boolean apply(NodeMetadata t) {
				return nodes.contains(t);
			}
		});
		context.close();
	}

	public final T syncDeploy(String... args) {
		ExecutorService e = Executors.newCachedThreadPool();
		for (NodeMetadata n : nodes) {
			e.submit(new LaunchTask(n, this.getClass().getName(), args));
		}
		e.shutdown();
		return getRemoteReference();
	}

	protected abstract T getRemoteReference();

	private class LaunchTask implements Runnable {

		private final String mainClass;
		private final NodeMetadata nm;
		private final String[] args;

		LaunchTask(NodeMetadata nm, String mainClass, String[] args) {
			this.nm = nm;
			this.mainClass = mainClass;
			this.args = args;
		}

		public void run() {
			SshClient ssh = context.utils().sshForNode().apply(nm);
			try { //TODO upload api metadata
				ssh.connect();
				ssh.put("~/" + JARNAME, Payloads.newPayload(jar));
				ssh.execChannel("java -server -cp " + JARNAME
					+ " -Djava.security.policy=policy "
					+ heapSize(nm.getHardware().getRam())
					+ " " + mainClass + " "
					+ args[0]);
			} finally {
				if (ssh != null) {
					ssh.disconnect();
				}
			}
		}
	}

	private String heapSize(int ram) {
		ram = (ram * 19) / 20;
		return "-Xmx" + ram + "m -Xms" + ram + "m ";
	}
}

package cn.gov.cma.guilin.workhelper.model;

public class Work {
	private String fileName;//文件名
	private String beginTime;//放球时间
	private String endTime;//终止时间
	private String airStopReason;//探空终止原因
	private String windStopReason;//测风终止原因
	private String airStopHight;//探空终止高度
	private String windStopHight;//测风终止高度
	private String calculator;//计算者
	private String checktor;//校对者
	private String approvetor;//预审者
	/**
	 * 7点和19点是主班次数，对应计算者，默认为true
	 * 如果为false,将对应01点是主班次数，对应校验者
	 */
	private boolean nomalMajor =  true;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getAirStopReason() {
		return airStopReason;
	}
	public void setAirStopReason(String airStopReason) {
		this.airStopReason = airStopReason;
	}
	public String getWindStopReason() {
		return windStopReason;
	}
	public void setWindStopReason(String windStopReason) {
		this.windStopReason = windStopReason;
	}
	public String getAirStopHight() {
		return airStopHight;
	}
	public void setAirStopHight(String airStopHight) {
		this.airStopHight = airStopHight;
	}
	public String getWindStopHight() {
		return windStopHight;
	}
	public void setWindStopHight(String windStopHight) {
		this.windStopHight = windStopHight;
	}
	public String getCalculator() {
		return calculator;
	}
	public void setCalculator(String calculator) {
		this.calculator = calculator;
	}
	public String getChecktor() {
		return checktor;
	}
	public void setChecktor(String checktor) {
		this.checktor = checktor;
	}
	public String getApprovetor() {
		return approvetor;
	}
	public void setApprovetor(String approvetor) {
		this.approvetor = approvetor;
	}
	public boolean isNomalMajor() {
		return nomalMajor;
	}
	public void setNomalMajor(boolean nomalMajor) {
		this.nomalMajor = nomalMajor;
	}
	@Override
	public String toString() {
		return "Work [fileName=" + fileName + ", beginTime=" + beginTime + ", endTime=" + endTime + ", airStopReason="
				+ airStopReason + ", windStopReason=" + windStopReason + ", airStopHight=" + airStopHight
				+ ", windStopHight=" + windStopHight + ", calculator=" + calculator + ", checktor=" + checktor
				+ ", approvetor=" + approvetor + "]";
	}
}

import { Book, Github, type LucideIcon, MessageSquare, Video } from "lucide-react";

type ResourceType = "documentation" | "community & support" | "video/tutorial" | "github";

export class ResourceItem {
  public readonly title: string;
  public readonly url:   string;
  public readonly description: string;
  public readonly type: ResourceType;

  constructor(title: string, url: string, description: string, type: ResourceType) {
    this.title = title;
    this.url   = url;
    this.description = description;
    this.type = type;
  }

  getDisplayType(): string {
    switch (this.type) {
      case "documentation":
      case "github":
        return this.type.charAt(0).toUpperCase() + this.type.slice(1);
      case "community & support":
        return "Communauté & Support";
      case "video/tutorial":
        return "Vidéo ou Tutoriel";
    }
  }

  getIcon(): LucideIcon {
    switch (this.type) {
      case "documentation":
        return Book;
      case "community & support":
        return MessageSquare;
      case "video/tutorial":
        return Video;
      case "github":
        return Github;
    }
  }

  getBadgeClassName(): string {
     switch (this.type) {
       case "documentation":
         return "bg-blue-500/10 text-blue-500";
       case "community & support":
         return "bg-green-500/10 text-green-500";
       case "video/tutorial":
         return "bg-red-500/10 text-red-500";
       case "github":
         return "bg-purple-500/10 text-purple-500";
     }
   }
}

export const helpResourcesData: ResourceItem[] = [
  new ResourceItem(
    "Documentation Java",
    "https://docs.oracle.com/en/java/",
    "Documentation officielle de Java par Oracle",
    "documentation"
  ),
  new ResourceItem(
    "Documentation Jakarta EE",
    "https://jakarta.ee/specifications/",
    "Spécifications officielles de Jakarta EE",
    "documentation"
  ),
  new ResourceItem(
    "Documentation Jakarta Servlet",
    "https://jakarta.ee/specifications/servlet/",
    "Documentation des servlets Jakarta",
    "documentation"
  ),
  
];
